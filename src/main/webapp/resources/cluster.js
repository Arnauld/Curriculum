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
});

var cluster = {
    logMaxSize: 10,

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
            cluster.log(data);
          },
          dataType: "json"
        });
    },

    formatMessage: function(json) {
        var $root = $("<div/>").addClass("message");
        var $type = $("<div/>").addClass("type").addClass(json.type).html("&nbsp;");
        var $cont = $("<div/>").addClass("content").html(json.message);
        $root.append($type).append($cont);
        return $root;
    }
};

