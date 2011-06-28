$(document).ready(function() {
	if(false)
    $(".skill input").each(function(index) {
        var $this = $(this);
        var amount = $this.val();
        var progress = $("<div/>");
        $this.after(progress);
        progress.progressbar({ value: 10*amount });
        progress.animate({
          opacity: .5,
          height: '50%'
        },
        {
          step: function(now, fx) {
            var data = fx.elem.id + ' ' + fx.prop + ': ' + now;
            $('body').append('<div>' + data + '</div>');
          }
        })
    });
});
