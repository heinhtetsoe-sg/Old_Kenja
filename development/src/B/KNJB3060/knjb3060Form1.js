function btn_submit(cmd) {
	if(!confirm('{rval MSG101}')){
		return;
	}
	var rengeStart = new Date(Date.parse(document.forms[0].RENGE_START.value));
	var rengeEnd = new Date(Date.parse(document.forms[0].RENGE_END.value));
	if($('select[name=REFLECTDIV]').val() == '3'){
		if(!$('input[name=WEEK_RADIO]:checked').val()){
			alert('曜日が選択されていません。');
			return false;
		}
		
		var list = new Array();
		for (var i = 1; i <= 12; i++) {
			var dayText = $('#week_table_' + i)[0].innerText;
			if(dayText){
				var dayDataTime = new Date(Date.parse(dayText));
				if(dayDataTime<rengeStart){
					continue;
				}
				if(dayDataTime>rengeEnd){
					continue;
				}
				var flag = false;
				for (var j = 0; j < list.length; j++) {
					if (list[j] == dayText) {
						flag = true;
						break;
					}
				}
				if(!flag){
					list.push(dayText);
				}
			}
		}
		
		if(list.length == 0){
			alert('有効な日付が選択されていません。');
			return false;
		}
		list.sort();
		document.forms[0].WEEK_TABLE_DAYS.value = list.join(',');
	} else {
		var startDate = new Date(Date.parse($('input[name=START_DATE]').val()));
		var endDate = new Date(Date.parse($('input[name=END_DATE]').val()));
		if(startDate>endDate){
			alert('有効な日付が選択されていません。');
			return false;
		}
		if(startDate<rengeStart || startDate>rengeEnd){
			alert('有効な日付が選択されていません。');
			return false;
		}
		if(endDate<rengeStart || endDate>rengeEnd){
			alert('有効な日付が選択されていません。');
			return false;
		}
	}
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//更新種別変更
function refrectDivChange(){
	var calendarBox = $('#reflactDivBox1 #dwindow')[0];
	if(calendarBox){
		$('#CalendarBox')[0].innerHTML = calendarBox.outerHTML;
		$('#reflactDivBox1 #dwindow')[0].outerHTML = '';
	}
	if($('select[name=REFLECTDIV]').val() != '3'){
		$('#reflactDivBox1').show();
		$('#reflactDivBox2').hide();
	} else {
		$('#reflactDivBox1').hide();
		$('#reflactDivBox2').show();
	}
}
//ダイアログ表示
//ダイアログ内のIDのプレフィックスにdef_は使わないこと。
function showDialog(contentsId, title, callback) {
    var srcDialogBox = $('#def_'+contentsId)[0];
    if (!srcDialogBox) {
        var srcDialogBoxHTML = $('#'+contentsId)[0].outerHTML;
        //ダイアログ内のid="××" → id="def_××"に変更。要はコピー作成
        srcDialogBoxNameHTML = srcDialogBoxHTML.replace(/(<[^>]*name=")([^"]+)("[^>]*>)/g , '$1def_$2$3');
        $('#'+contentsId)[0].outerHTML = srcDialogBoxNameHTML.replace(/(<[^>]*id=")([^"]+)("[^>]*>)/g , '$1def_$2$3');
    } else {
        var srcDialogBoxHTML = $('#def_'+contentsId)[0].outerHTML.replace(/(<[^>]*name=")def_([^"]+)("[^>]*>)/g , '$1$2$3');
        var srcDialogBoxHTML = srcDialogBoxHTML.replace(/(<[^>]*id=")def_([^"]+)("[^>]*>)/g , '$1$2$3');
    }

    //dialogBox.dialogBoxContentsの中身を書き換えている
    $('#dialogBoxContents')[0].innerHTML = srcDialogBoxHTML;
    $('#dialogBoxTitle')[0].innerHTML = title;
    $('#'+contentsId).show();
    $('#dialogBox').show();
    
    // ブラウザの横幅を取得(全体の幅からダイアログの幅を引いて半分。要は中央ぞろえ)
    var browserWidth = $(window).width();
    var boxW = $("#dialogBoxTable").width();
    var plusPxW = ((browserWidth - boxW) / 2);
    var browserHeight = $(window).height();
    var boxH = $("#dialogBoxTable").height();
    var plusPxH = ((browserHeight - boxH)/2);
    if (plusPxH < 0) {
        plusPxH = 0;
    }

    //dialogBox見えないDivを全画面に展開（表示したダイアログ以外触れないようにする）
    $('#dialogBox').css({'left':0});
    $('#dialogBox').css({'top': 0});
    $('#dialogBox').css({'width': $(document).width() + "px"});
    $('#dialogBox').css({'height': $(document).height() + "px"});

    //ダイアログ
    $('#dialogBoxTable').css({'position': 'absolute'});
    $('#dialogBoxTable').css({'left': plusPxW + "px"});
    $('#dialogBoxTable').css({'top': plusPxH + "px"});

    //タイトルの部分をドラックして移動できるようにしている。
    $('#dialogTitleBar').mousedown(function(e){
        e.preventDefault();
        //dialogBoxTableの中にdataを使って変数を作成(最初の座標保持)
        $('#dialogBoxTable')
            .data("clickPointX" , e.pageX - $('#dialogTitleBar').offset().left)
            .data("clickPointY" , e.pageY - $('#dialogTitleBar').offset().top);
        //dialogBoxTableのmousedown時の座標と現在の座標を使用してダイアログの位置を動かす
        //激しく動かしてダイアログからマウスが外れても動く動くようにdocument.mousemoveにしてる
        $(document).mousemove(function(e){
            e.preventDefault();
            $('#dialogBoxTable').css({
                top:e.pageY - $('#dialogBoxTable').data("clickPointY")+"px",
                left:e.pageX - $('#dialogBoxTable').data("clickPointX")+"px"
            })
        })
    }).mouseup(function(e){
        e.preventDefault();
        $(document).unbind("mousemove");
    });
    if (callback) {
        callback();
    }
}
//曜日指定-クリア処理
function weekClear(){
	for (var i = 1; i <= 12; i++) {
		$('#week_table_' + i)[0].innerText = '';
	}
}
//曜日指定-並び替え処理
function weekSort(){
	var list = new Array();
	for (var i = 1; i <= 12; i++) {
		var dayText = $('#week_table_'+i)[0].innerText;
		if (dayText) {
			var flag = false;
			for (var j = 0; j < list.length; j++) {
				if (list[j] == dayText) {
					flag = true;
					break;
				}
			}
			if(!flag){
				list.push(dayText);
			}
		}
	}
	console.log(list);
	list.sort();
	for (var i = 1; i <= 12; i++) {
		if(list[i-1]){
			$('#week_table_'+i)[0].innerText = list[i-1];
		} else {
			$('#week_table_'+i)[0].innerText = '';
		}
	}
}
var selectedId;
//テーブルセルクリック
function weekTableAction(obj){
	selectedId=obj.id;
	showDialog('daySelect','日付選択',daySelectInitFunc)
}
//日付選択ダイアログ初期化
function daySelectInitFunc(){
	var targetDay = $('#'+selectedId)[0].innerHTML;
	if(targetDay){
		$('input[name=SELECT_DATE]').val(targetDay);
	}
	$('input[name=btn_calen]').off();
	$('input[name=btn_calen]').on('click',function(){
		$('#dwindow').css('z-index',2000);
	});
}
//日付選択ダイアログOKボタン処理
function daySelect_OK(){
	$('#'+selectedId)[0].innerHTML = $('input[name=SELECT_DATE]').val();
	$('#dialogBox').hide();
}
//日付選択ダイアログクリアボタン処理
function daySelect_Clear(){
	$('#'+selectedId)[0].innerHTML = '';
	$('#dialogBox').hide();
}
//日付選択セルマウスオーバー処理
function weekSetColor(obj){
	$('#'+obj.id).css('background-color','#FFCCCC');
}
//日付選択セルマウスアウト処理
function weekClearColor(){
	for (var i = 1; i <= 12; i++) {
		$('#week_table_' + i).css('background-color','');
	}
}
$(window).on('load',function(){
    refrectDivChange();
});
