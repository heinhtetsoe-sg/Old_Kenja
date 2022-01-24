// ツールチップ
function Tooltip()
{
    // 内容
    this.content = document.createElement( 'div' );
    this.content.className = 'tooltip-content';

    // 影
    this.shadow = document.createElement( 'div' );
    this.shadow.className = 'tooltip-shadow';

    this.shadow.appendChild( this.content );
}

Tooltip.DELAY = 100;    // 表示するまでの遅延時間
Tooltip.OFFSET = 5;     // マウスポインタからのオフセット

// ツールチップを表示する
Tooltip.prototype.Show = function( text, x, y )
{
    // 内容
    while( this.content.hasChildNodes() )
    {
        this.content.removeChild( this.content.lastChild );
    }
    this.content.appendChild( document.createTextNode( text ) );

    // 影
    this.shadow.style.left = x + 'px';
    this.shadow.style.top = y + 'px';
    this.shadow.style.visibility = 'visible';


    if( this.shadow.parentNode != document.body )
    {
        // ドキュメントのbody要素に追加する
        document.body.appendChild( this.shadow );
    }
}

Tooltip.prototype.Hide = function()
{
    this.shadow.style.visibility = 'hidden';
}
// ツールチップの表示を予定する
Tooltip.prototype.Schedule = function( targetElement, event )
{
    var e = event || window.event;

    var x = e.clientX;
    var y = e.clientY;

    // マウスポインタの位置をドュメント座標に正する
    x += window.pageXOffset || document.documentElement.scrollLeft;
    y += window.pageYOffset || document.documentElement.scrollTop;

    // イベントハンドラ内で処理できthisンす
    var _this = this;


    // タイムアウト処理を設定する
    var timerID = window.setTimeout(
        function()
        {
            var text = targetElement.getAttribute( 'tooltip' );

            // ツールチップを示す
            _this.Show(
                text,
                x + Tooltip.OFFSET,
                y + Tooltip.OFFSET
                );
        },
        Tooltip.DELAY
        );


    function MouseOut()
    {
        // ツールチップを隠す
        _this.Hide();

        // 未処理のタイムアウト処理を取消す
        window.clearTimeout( timerID );

        // イベントハンドラを削除する
        if( targetElement.removeEventListener )
        {
            targetElement.removeEventListener( 'mouseout', MouseOut, false );
        }
        else
        {
            // IE用の処理
            targetElement.detachEvent( 'onmouseout', MouseOut );
        }
    }

    // イベントハンドラを登録する
    if( targetElement.addEventListener )
    {
        targetElement.addEventListener( 'mouseout', MouseOut, false );
    }
    else
    {
        // IE用の処理
        targetElement.attachEvent( 'onmouseout', MouseOut );
    }
}

function btn_submit(cmd) {
    
    if(cmd == 'hyouzi'){
        if(document.forms[0].KYOKA.value == ""){
            alert('教科を選択してください。');
            document.forms[0].KYOKA.focus();
            //document.forms[0].YEAR.value = document.forms[0].BEFORE_YEAR.value;     //表示してた年度に戻す
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "DELCHK[]" && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}


function checkCng() {
    document.forms[0].cmd.value = 'kubun_change';
    document.forms[0].submit();
    return false;
}

function yearChange(year)
{
    
    document.forms[0].YEAR.value = year;
    if(document.forms[0].KYOKA.value != ""){
        btn_submit('hyouzi');
    }else{
        btn_submit('edit');
    }
}
