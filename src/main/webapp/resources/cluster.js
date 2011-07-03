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
        cluster.post("/cluster/start", {
                "start-node-name":"McCallum",
                "start-node-port":9001
         });
    });

    //
    cluster.waitForMessage();
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
        $msg.fadeIn(300);
    },

    post: function(_url,_data) {
        $.ajax({
          type: 'POST',
          url: _url,
          data: _data,
          success: function (data) {
            // no-op: message will be queried by the auto-polling scheduled
          },
          dataType: "json"
        });
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

    waitForMessage: function () {
        $.ajax({
            type: "GET",
            url: "/msg/list/"+cluster.lastMessage,
            data: {},

            async: true, /* If set to non-async, browser shows page as "Loading.."*/
            cache: false,
            timeout:50000, /* Timeout in ms */

            success: function(data){ /* called when request to barge.php completes */
                cluster.lastCallInError = false;
                $.each(data, function(key, val) {
                    alert("Received "+key+"/"+val+"!");
                    cluster.lastMessage = val.id;
                    cluster.log(val); /* Add response to a .msg div (with the "new" class)*/
                });
                setTimeout(
                    'cluster.waitForMessage()', /* Request next message */
                    1000 /* ..after 1 seconds */
                );
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                if(cluster.lastCallInError) {
                    // only log is the previous was not an error
                }
                else {
                    cluster.lastCallInError = true;
                    var message = textStatus
                    if(message=="error")
                        message = "Server probably down";
                    cluster.log({"type":"type-error", "message":message + " (" + errorThrown + ")"});
                }
                setTimeout(
                    'cluster.waitForMessage()', /* Try again after.. */
                    "15000"); /* milliseconds (15seconds) */
            },
        });
    }
};


