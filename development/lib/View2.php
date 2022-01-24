<?php

require_once('for_php7.php');

require_once("htmltemplate.inc");
/* 参加者ログ（自動作成します）*/
define("USR_LST", "/tmp/gaku/user.dat");
/* リストに何秒間残すか */
define("TIMEOUT", 300);
function htmlspecialchars_array(&$item, $key) {
    $item = htmlspecialchars($item);
}
class View
{
    //MAIN共通
    function toHTML(&$model, $file, $data)
    {
        //Onlineユーザー数
        if(!file_exists(USR_LST)){
            $nf = fopen(USR_LST, "w");
            fclose($nf);
        }
        //Javascriptファイル名
        $jsFile = str_replace(".html", ".js", basename($file));
        //リストの幅
        if (!isset($data["SEL_WIDTH"])) $data["SEL_WIDTH"] = "60%";
        $args = array("BODY"                =>HtmlTemplate::t_buffer($file, $data),
                      "HEADER"              =>(isset($data["HEADER"]))? $data["HEADER"] : '',
                      "JAVASCRIPT"          =>((isset($data["JAVASCRIPT"]))? $data["JAVASCRIPT"] : '') .View::jsMessage($jsFile),
                      "COMMON_JS_FILE"      =>COMMON_JS_FILE,
                      "CSS_FILE"            =>CSS_FILE,
                      "TITLE"               =>(isset($data["TITLE"]))? $data["TITLE"] : TITLE,
                      "CHARSET"             =>CHARSET,
                      "AUTHORITY"           =>AUTHORITY,
                      "PROGRAMID"           =>PROGRAMID,
                      "COPYRIGHT"           =>COPYRIGHT,
                      "ERROR_MESSAGE"       => View::showAlert($model)
                      );

        HtmlTemplate::t_include(TMPLDIRECTORY ."/main.html",$args);
    }
    function CheckUser($addr,$userid=""){
        $usr_arr = file(USR_LST);
        $fp = fopen(USR_LST, "w");
        $now = time();
        for($i = 0; $i < get_count($usr_arr); $i++){
            list($ip_addr,$usr,$tim_stmp) = explode("|", $usr_arr[$i]);
            if(($now-$tim_stmp) < TIMEOUT){
                if($ip_addr != $addr){
                    fputs($fp, "$ip_addr|$usr|$tim_stmp");
                }
            }
        }
        fputs($fp, "$addr|$userid|$now\n");
        fclose($fp);
    }
    function UserCount(){
        $usr_arr = file(USR_LST);
        return get_count($usr_arr);
    }
    //JavaScriptのメッセージ取得
    function jsMessage($file)
    {
        if (file_exists($file)){
            $js = fread(fopen($file,"rb"),filesize($file));
            if (!empty($js)){
                $m = array();
                if (preg_match_all('/\{rval ([^\}]+)\}/',$js, $regs)){
                    $m = $regs[1];
                }
                if (preg_match_all('/\{val ([^\}]+)\}/',$js, $regs)){
                    $m = array_merge($m, $regs[1]);
                }
                if (get_count($m) > 0){
                    $db = Query::dbCheckOut();
                    $query = "";
                    $query .= "SELECT ";
                    $query .= "  * ";
                    $query .= "FROM ";
                    $query .= "  MESSAGE_MST ";
                    $query .= "WHERE ";
                    $query .= "  MSG_CD IN ('" .implode($m, "','") ."') ";

                    $result = $db->query($query);
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        $msg = str_replace("\r","\\r",$row["MSG_CONTENT"]);
                        $msg = str_replace("\n","\\r",$msg);
                        $msg = $row["MSG_CD"] ."\\r\\n\\r\\n" .$msg;
                        $js = str_replace("{val " .$row["MSG_CD"] ."}", $msg, $js);
                        $js = str_replace("{rval " .$row["MSG_CD"] ."}", $msg, $js);
                    }
                    Query::dbCheckIn($db);
                }
                return $js;
            }
        }
        return "";
    }
    //エラーメッセージ表示
    function &errorMessage($code)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM MESSAGE_MST"
                ." WHERE MSG_CD  = '" .$code ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        $row["MSG_CONTENT"] = str_replace("\r","",$row["MSG_CONTENT"]);
        $row["MSG_CONTENT"] = str_replace("\n","",$row["MSG_CONTENT"]);
        return $row["MSG_CONTENT"];
    }
    function &ShowMessage($code, $msg='')
    {
        return View::alert(View::errorMessage($code));
    }
    function alert($msg)
    {
        $str .= "<script language='javascript'>\n";
        $str .= "   alert('$msg')\n";
        $str .= "</script>";
        return $str;
    }
    //ユーザー毎のテンプレート
    function t_include($file, $args)
    {
        HtmlTemplate::t_include($file, $args);
    }
    //フレーム作成
    function frame($args, $file='frame.html',$border=1)
    {
        $tmp = array("CSS_FILE"    =>CSS_FILE,
                      "TITLE"       =>TITLE,
                      "CHARSET"     =>CHARSET,
                      "BORDER"     =>$border
                      );

        $args = array_merge($args, $tmp);

//        $args["top_src"] = REQUESTROOT ."/common/hidden.php";
        HtmlTemplate::t_include(TMPLDIRECTORY ."/" .$file, $args);
    }
    //アラートを表示
    function showAlert(&$model)
    {
        $alert = "";
        if (isset($model->warning)){
            $msg = $model->warning;
        }elseif (isset($model->message)){
            $msg = $model->message;
        }
        return $msg;
    }
    //学習記録エクスプローラー
    function popUpGtre(&$form)
    {
        //読込ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_popup",
                        "value"       => "･･･",
                        "extrahtml"   => "onclick=\"wopen('" .REQUESTROOT ."/X/KNJXGTRE/index.php','KNJXGTRE',0,0,900,550)\"") );

        return  $form->ge("btn_popup");
    }
    //インラインフレーム用Javascriptタグ生成
    function setIframeJs(){
        static $iFlag = false;
        if (!$iFlag){
            $imgpath = REQUESTROOT ."/image/system";
            $REQUESTROOT = REQUESTROOT;
            $html = <<<EOP
            <script language="JavaScript" src="$REQUESTROOT/common/iframe.js"></script>
            <div id="dwindow" style="position:absolute;z-index:2;background-color:navy;cursor:hand;left:0;top:0;display:none" onMousedown="initializedrag(event)" onSelectStart="return false">
            <div align="right"><img src="$imgpath/max.gif" id="maxname" onClick="maximize()"><img src="$imgpath/close.gif" onClick="closeit()"></div>
            <iframe id="cframe" name="iframe" src="" width=100% height=100%></iframe>
            <div id="saver" style="width:100%;height:100%;position:absolute;left:0;top:0;display:none"></div>

            </div>
EOP;
            $iFlag = true;
        }
        return $html;
    }
    //カレンダーコントロール
    function popUpCalendar(&$form, $name, $value="",$obj,$param="")
    {
        global $sess;
        //テキストエリア
        $form->ae( array("type"        => "text",
                        "name"        => $name,
                        "size"        => 12,
                        "maxlength"   => 12,
                        "extrahtml"   => "onblur=\"isDate(this)\"",
                        "value"       => $value));

        //読込ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_calen",
                        "value"       => "･･･",
                        "extrahtml"   => "onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=$name&frame='+getFrameName(self) + '&date=' + obj.value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"") );

        return View::setIframeJs() .$form->ge($name) .$form->ge("btn_calen");
    }

/*    //カレンダーコントロール
    function popUpCalendar(&$form, $name, $value="",$param="")
    {
        $param = "name=$name&" .$param;
        //テキストエリア
        $form->ae( array("type"        => "text",
                        "name"        => $name,
                        "size"        => 12,
                        "maxlength"   => 12,
                        "extrahtml"   => "onblur=\"isDate(this)\"",
                        "value"       => $value));

        //読込ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_calen",
                        "value"       => "...",
                        "extrahtml"   => "onclick=\"subWinOpen_cal('$name','$param');\"") );

        return  $form->ge($name) .$form->ge("btn_calen");
    }
*/
/*  郵便番号入力支援
    例)
    $arg["ADDRESS"] = View::popUpZipCode($objForm, "address");
*/
    function popUpZipCode(&$form, $zipname, $value="", $name="", $size=10)
    {
        global $sess;
        //テキストエリア
        $form->ae( array("type"        => "text",
                        "name"        => $zipname,
                        "size"        => $size,
                        "extrahtml"   => "onblur=\"isZipcd(this)\"",
                        "value"       => $value));

        //読込ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_zip",
                        "value"       => "郵便番号入力支援",
                        "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=$name&zipname=$zipname&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );

        //確定ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_apply",
                        "value"       => "確定",
                        "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=$name&zipname=$zipname&zip='+document.forms[0]['$zipname'].value+'&frame='+getFrameName(self))\"") );

        return  View::setIframeJs() .$form->ge($zipname) .$form->ge("btn_zip") .$form->ge("btn_apply");
    }

/*    function popUpZipCode(&$form, $zipname, $value="", $name="", $size=10)
    {
        //テキストエリア
        $form->ae( array("type"        => "text",
                        "name"        => $zipname,
                        "size"        => $size,
                        "extrahtml"   => "",
                        "value"       => $value));

        //読込ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_zip",
                        "value"       => "郵便番号入力支援",
                        "extrahtml"   => "onClick=\"subWinOpen_zip('$name','$zipname');\"" ) );

        return  $form->ge($zipname) .$form->ge("btn_zip");
    }
*/
/*
    生徒番号入力支援
    例)
    $arg["ADDRESS"] = View::popUpStuCode($objForm, "address");

    function popUpStuCode(&$form, $targetname, $value="", $name="生徒検索", $size=6)
    {
        //テキストエリア
        $form->ae( array("type"        => "text",
                        "name"        => $targetname,
                        "size"        => ($size + 1),
                        "maxlength"   => $size,
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                        "value"       => $value));

        //読込ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_stu",
                        "value"       => $name,
                        "extrahtml"   => "onClick=\"subWinOpen_stu('$targetname');\"" ) );

        return  $form->ge($targetname) .$form->ge("btn_stu");
    }
*/
    // $aHREF       : リンク先の URL から URL パラメータを除いた部分
    // $aStr        : 表示用文字列
    // $aAdditional : a タグへの追加属性
    // $aHash       : URL パラメータ生成用の連想配列
    function alink($aHREF, $aStr, $aAdditional = "", $aHash = "" ) {
        $href = $aHREF;
        if( is_array( $aHash ) ) {
            $href .= "?";
            $arrParam = array();
            foreach($aHash as $key => $val ) {
                $arrParam[] = urlencode($key) . "=" . urlencode($val);
            }
            $href .= join( '&', $arrParam );
        }
        return "<a href=\"$href\"". ($aAdditional ? " $aAdditional" : ""). ">" .
             $aStr . "</a>";
    }
/*
    更新後前の生徒へボタン作成
    引数１：モデルオブジェクト変数
    引数２：フォームオブジェクト変数
    引数３：更新時のボタン名
*/
    function updateNext(&$model, &$objForm, $btn='btn_update'){
        //更新ボタン
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" onclick=\"top.main_frame.left_frame.updateNext(self, 'pre','".$btn ."');\""));

        //更新ボタン
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" onclick=\"top.main_frame.left_frame.updateNext(self, 'next','".$btn ."');\""));

        if ($_POST["_ORDER"] == "pre" || $_POST["_ORDER"] == "next" ){
           $order = $_POST["_ORDER"];
           if (!isset($model->warning)){
               echo <<<EOP
                   <script language="javascript">                    
                       top.main_frame.left_frame.nextLink('$order');
                   </script>
   
EOP;
                unset($model->message);
                exit;
           }
        }
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "_ORDER" ));
                        
        return $objForm->ge("btn_up_pre") .$objForm->ge("btn_up_next");
    }

}
?>
