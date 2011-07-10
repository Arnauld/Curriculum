$(function() {
    $('#nav > div').hover(
    function () {
        var $this = $(this);
        $this.find('img').stop().animate({
            'width'     :'199px',
            'height'    :'199px',
            'top'       :'-25px',
            'left'      :'-25px',
            'opacity'   :'1.0'
        },500,'easeOutBack',function(){
            $(this).parent().find('ul').fadeIn(700);
        });

        $this.find('a:first,h2').addClass('active');
    },
    function () {
        var $this = $(this);
        $this.find('ul').fadeOut(500);
        $this.find('img').stop().animate({
            'width'     :'52px',
            'height'    :'52px',
            'top'       :'0px',
            'left'      :'0px',
            'opacity'   :'0.1'
        },5000,'easeOutBack');

        $this.find('a:first,h2').removeClass('active');
    });

    $("#start-node").click(function () {
        cluster.send('POST', "/cluster/start", {
                "start-node-name":"McCallum",
                "start-node-port":9001
         });
    });
    
    $("#search").click(function () {
        cluster.send('GET', "/search", {
                "entity":"curriculum_vitae",
                "instance.potential": "100%"
         });
    })

    // start polling
    cluster.startPolling();
});

var cluster = {
    logMaxSize: 10,

    lastMessage: 0,

    log: function(message) {
        var $msg = cluster.formatMessage(message);
        $msg.hide();

        var messages = $("#logs div.message");
        // remove the tops elements
        if(messages.length>=cluster.logMaxSize) {
            var overflow = messages.slice(0, messages.length-cluster.logMaxSize+1);
            overflow.fadeOut(300, function () {
                $(this).remove();
            });
        }
        $("#logs").append($msg);
        $msg.hover(function () {
            $(this).addClass("hover");
        },
        function() {
            $(this).removeClass("hover");
        })
        $msg.fadeIn(300);
        $msg.find(".instance-link").click(function() {
            var $this = $(this);
            $("#display").empty();
            $("#display").append("<iframe src='" + $this.attr("href") + "' width='630px' height='800px'></iframe>");
            $("#logs").stop().animate({"left":"650px"}, 1000, "swing");
            return false;
        });
    },

    send: function(_method, _url,_data) {
        $.ajax({
          type: _method,
          url: _url,
          data: _data,
          success: function (data) {
            // no-op: message will be queried by the auto-polling scheduled
          },
          dataType: "json"
        });

        cluster.numberOfCallsWithoutMessage = 0;//prevent wait
    },

    defaultMsg: {
        "id":"-1",
        "type":"type-unknown",
        "message":"?"
    },

    formatMessage: function(json) {
        var msg = $.extend({}, cluster.defaultMsg, json);
        var $root = $("<div/>").addClass("message").attr("message_id", msg.id);
        var $type = $("<div/>").addClass("type").addClass(msg.type).html("&nbsp;");
        var $cont = $("<div/>").addClass("content").html(msg.message);
        $root.append($type).append($cont);
        return $root;
    },

    lastCallInError: false,

    numberOfCallsWithoutMessage: 0,

    isPolling: false,

    checkPeriodicity: "1000", // every 1 second

    tickCount: 0,

    startPolling: function () {
        setInterval("cluster.waitForMessage()", cluster.checkPeriodicity);
    },

    /*
     * Probably not the most efficient way to do this
     * TODO: replace by long polling
     */
    waitForMessage: function () {
        console.log("waitForMessage isPolling: " + cluster.isPolling);
        if(cluster.isPolling)
            return;
        cluster.isPolling = true;

        cluster.tickCount = cluster.tickCount + 1;
        var elligible =   (cluster.numberOfCallsWithoutMessage==0)
                        ||(cluster.numberOfCallsWithoutMessage<2 && cluster.tickCount%2 == 0)
                        ||(cluster.numberOfCallsWithoutMessage<5 && cluster.tickCount%5 == 0)
                        ||(cluster.tickCount%10 == 0);

        console.log("waitForMessage elligible: " + elligible);
        if(elligible)
            cluster.pollMessages();
        cluster.isPolling = false;
    },

    pollMessages: function () {
        console.log("pollMessages begin");
        $.ajax({
            type: "GET",
            url: "/msg/list/"+cluster.lastMessage,
            data: {},

            async: true, /* If set to non-async, browser shows page as "Loading.."*/
            cache: false,
            timeout:50000, /* Timeout in ms */

            success: function(data) {
                var retrieved = 0;
                cluster.lastCallInError = false;
                $.each(data, function(key, val) {
                    retrieved = retrieved + 1;
                    cluster.lastMessage = val.id;
                    cluster.log(val);
                });

                if(retrieved>0)
                    cluster.numberOfCallsWithoutMessage = 0; //prevent wait
                else
                    cluster.numberOfCallsWithoutMessage = cluster.numberOfCallsWithoutMessage + 1;
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                if(cluster.lastCallInError) {
                    // only log if the previous was not an error
                }
                else {
                    cluster.lastCallInError = true;
                    var message = textStatus
                    if(message=="error")
                        message = "Server probably down";
                    cluster.log({"type":"type-error", "message":message + " (" + errorThrown + ")"});
                }
            },
        });
    }
};


