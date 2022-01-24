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
    function toHTML(&$model, $file, $data, $notChangeSize = "")
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
        $args = array("BODY"                    =>HtmlTemplate::t_buffer($file, $data),
                      "HEADER"                  =>(isset($data["HEADER"]))? $data["HEADER"] : '',
                      "JAVASCRIPT"              =>((isset($data["JAVASCRIPT"]))? $data["JAVASCRIPT"] : '') .View::jsMessage($jsFile),
                      "COMMON_JS_FILE"          =>COMMON_JS_FILE,
                      "COMMON_JS_PROTO"         =>COMMON_JS_PROTO,
                      "COMMON_JS_EFFECT"        =>COMMON_JS_EFFECT,
                      "COMMON_JS_SMARTDIALOG"   =>COMMON_JS_SMARTDIALOG,
                      "COMMON_TABLE_SORT"       =>COMMON_TABLE_SORT,
                      "CSS_FILE"                =>CSS_FILE,
                      "CSS_FILE2"               =>CSS_FILE2,
                      "JQUERY"                  =>JQUERY,
                      "UI_CORE"                 =>UI_CORE,
                      "TITLE"                   =>(isset($data["TITLE"]))? $data["TITLE"] : TITLE,
                      "CHARSET"                 =>CHARSET,
                      "AUTHORITY"               =>AUTHORITY,
                      "PROGRAMID"               =>PROGRAMID,
                      "COPYRIGHT"               =>COPYRIGHT,
                      "ERROR_MESSAGE"           => View::showAlert($model)
                      );
        //DBからgk.cssの種類を変更したい
        $db = Query::dbCheckOut();
        $staffQuery = " SELECT FIELD1, FIELD2 FROM STAFF_DETAIL_SEQ_MST WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
        $staffRow = $db->getRow($staffQuery, DB_FETCHMODE_ASSOC);

        $cssNo = $staffRow["FIELD1"];

        if($staffRow["FIELD2"] != "2" || $notChangeSize){
            $size = "";
        }else{
            $size = "big";
        }

        if($cssNo == ""){
            $cssNo = 1;
        }
        $file_name = DOCUMENTROOT."/common/gkcss/gk".$cssNo.$size.".css";
        //ファイルの更新日時取得
        $date = View::echo_filedate($file_name);

        $args["CSS_FILE"] = REQUESTROOT."/common/gkcss/gk".$cssNo.$size.".css?date=".$date;;
        Query::dbCheckIn($db);

        HtmlTemplate::t_include(TMPLDIRECTORY ."/main.html",$args);
    }
    //MAIN共通
    function toHTML2(&$model, $file, $data)
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
                      "COMMON_TABLE_SORT"   =>COMMON_TABLE_SORT,
                      "COMMON_JS_PROTO"         =>COMMON_JS_PROTO,
                      "COMMON_JS_EFFECT"        =>COMMON_JS_EFFECT,
                      "COMMON_JS_SMARTDIALOG"   =>COMMON_JS_SMARTDIALOG,
                      "CSS_FILE"            =>CSS_FILE,
                      "CSS_FILE2"           =>CSS_FILE2,
                      "JQUERY"              =>JQUERY,
                      "UI_CORE"             =>UI_CORE,
                      "TITLE"               =>(isset($data["TITLE"]))? $data["TITLE"] : TITLE,
                      "CHARSET"             =>CHARSET,
                      "AUTHORITY"           =>AUTHORITY,
                      "PROGRAMID"           =>PROGRAMID,
                      "COPYRIGHT"           =>COPYRIGHT,
                      "ERROR_MESSAGE"       => View::showAlert($model)
                      );

        HtmlTemplate::t_include(TMPLDIRECTORY ."/main2.html",$args);
    }
    //MAIN共通
    function toHTML3(&$model, $file, $data)
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
                      "COMMON_TABLE_SORT"   =>COMMON_TABLE_SORT,
                      "COMMON_JS_PROTO"         =>COMMON_JS_PROTO,
                      "COMMON_JS_EFFECT"        =>COMMON_JS_EFFECT,
                      "COMMON_JS_SMARTDIALOG"   =>COMMON_JS_SMARTDIALOG,
                      "CSS_FILE"            =>CSS_FILE,
                      "CSS_FILE2"           =>CSS_FILE2,
                      "JQUERY"              =>JQUERY,
                      "UI_CORE"             =>UI_CORE,
                      "TITLE"               =>(isset($data["TITLE"]))? $data["TITLE"] : TITLE,
                      "CHARSET"             =>CHARSET,
                      "AUTHORITY"           =>AUTHORITY,
                      "PROGRAMID"           =>PROGRAMID,
                      "COPYRIGHT"           =>COPYRIGHT,
                      "ERROR_MESSAGE"       => View::showAlert($model)
                      );

        HtmlTemplate::t_include(TMPLDIRECTORY ."/main3.html",$args);
    }

    function toHTML4(&$model, $file, $data, $jsplugin="", $cssplugin="")
    {
        //Onlineユーザー数
        if(!file_exists(USR_LST)){
            $nf = fopen(USR_LST, "w");
            fclose($nf);
        }

        //20160427jsファイルの更新日時も取得
        $jsfilename = str_replace(REQUESTROOT,"",COMMON_JS_FILE);
        $commonjs = DOCUMENTROOT.$jsfilename;
        $jsdate = View::echo_filedate($commonjs);
        //Javascriptファイル名
        $jsFile = str_replace(".html", ".js", basename($file));
        //リストの幅
        if (!isset($data["SEL_WIDTH"])) $data["SEL_WIDTH"] = "60%";
        $args = array("BODY"                =>HtmlTemplate::t_buffer($file, $data),
                      "HEADER"              =>(isset($data["HEADER"]))? $data["HEADER"] : '',
                      "JAVASCRIPT"          =>((isset($data["JAVASCRIPT"]))? $data["JAVASCRIPT"] : '') .View::jsMessage($jsFile),
                      "COMMON_JS_FILE"      =>COMMON_JS_FILE."?date=".$jsdate,
                      //"COMMON_JS_FILE"      =>COMMON_JS_FILE,
                      //"OTHER_JS_FILE"       =>REQUESTROOT."/common/jquery.js",
                      //"OTHER_JS_FILE2"      =>REQUESTROOT."/common/boxover.js",
                      //"CSS_FILE"            =>CSS_FILE,//デフォルトでも使用しない
                      "COMMON_JS_PROTO"         =>COMMON_JS_PROTO,
                      "COMMON_JS_EFFECT"        =>COMMON_JS_EFFECT,
                      "COMMON_JS_SMARTDIALOG"   =>COMMON_JS_SMARTDIALOG,
                      "TITLE"               =>(isset($data["TITLE"]))? $data["TITLE"] : TITLE,
                      "CHARSET"             =>CHARSET,
                      "AUTHORITY"           =>AUTHORITY,
                      "PROGRAMID"           =>PROGRAMID,
                      "COPYRIGHT"           =>COPYRIGHT,
                      "ERROR_MESSAGE"       => View::showAlert($model)
                      );
       //*20100728 jspluginを配列化*
        if($jsplugin!=""){
           if(strstr($jsplugin,"|")){
              $jsplugin_row    = explode("|",$jsplugin);
          }else{
              $jsplugin_row[0] = $jsplugin;
          }
         $i=0;
         foreach($jsplugin_row as $key => $val)
         {
          $file_name = DOCUMENTROOT."/common/js/".$val;
          //ファイルの更新日時取得
          $date = View::echo_filedate($file_name);

          //$args["OTHER_JS_FILE".$i] = REQUESTROOT."/X/KNJXMENU/js/".$val;
          $args["OTHER_JS_FILE".$i] = REQUESTROOT."/common/js/".$val."?date=".$date;
          $i++;
          if($i==10){break;}
         }
        }
       //*20100728 csspluginを配列化*
        if($cssplugin!=""){
           if(strstr($cssplugin,"|")){
              $cssplugin_row    = explode("|",$cssplugin);
          }else{
              $cssplugin_row[0] = $cssplugin;
          }
         $i=0;
         foreach($cssplugin_row as $key => $val)
         {
          $file_name = DOCUMENTROOT."/common/kenja-design-sample/".$val;
          //ファイルの更新日時取得
          $date = View::echo_filedate($file_name);

          //$args["OTHER_CSS_FILE".$i] = REQUESTROOT."/X/KNJXMENU/kenja-design-sample/".$val;
          $args["OTHER_CSS_FILE".$i] = REQUESTROOT."/common/kenja-design-sample/".$val."?date=".$date;
          $i++;
          if($i==10){break;}
         }
        }

       // echo "@@@@args@@@@<br>";
       // new dBug($args);


        HtmlTemplate::t_include(TMPLDIRECTORY ."/main_test3.html",$args);
    }

    //MAIN共通
    function toHTML5(&$model, $file, $data)
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
        $args = array("BODY"                    =>HtmlTemplate::t_buffer($file, $data),
                      "HEADER"                  =>(isset($data["HEADER"]))? $data["HEADER"] : '',
                      "JAVASCRIPT"              =>((isset($data["JAVASCRIPT"]))? $data["JAVASCRIPT"] : '') .View::jsMessage($jsFile),
                      "COMMON_JS_FILE"          =>COMMON_JS_FILE,
                      "COMMON_JS_PROTO"         =>COMMON_JS_PROTO,
                      "COMMON_JS_EFFECT"        =>COMMON_JS_EFFECT,
                      "COMMON_JS_SMARTDIALOG"   =>COMMON_JS_SMARTDIALOG,
                      //"COMMON_TEXTAREAJS_FILE"  =>COMMON_TEXTAREAJS_FILE,
                      "COMMON_TABLE_SORT"       =>COMMON_TABLE_SORT,
                      "CSS_FILE"                =>CSS_FILE,
                      "JQUERY"                  =>JQUERY,
                      "UI_CORE"                 =>UI_CORE,
                      "TITLE"                   =>(isset($data["TITLE"]))? $data["TITLE"] : TITLE,
                      "CHARSET"                 =>CHARSET,
                      "AUTHORITY"               =>AUTHORITY,
                      "PROGRAMID"               =>PROGRAMID,
                      "COPYRIGHT"               =>COPYRIGHT,
                      "ERROR_MESSAGE"           => View::showAlert($model)
                      );

        //DBからgk.cssの種類を変更したい
        $db = Query::dbCheckOut();
        $staffQuery = " SELECT FIELD1, FIELD2 FROM STAFF_DETAIL_SEQ_MST WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
        $staffRow = $db->getRow($staffQuery, DB_FETCHMODE_ASSOC);

        $cssNo = $staffRow["FIELD1"];

        if($staffRow["FIELD2"] != "2" || $notChangeSize){
            $size = "";
        }else{
            $size = "big";
        }

        if($cssNo == ""){
            $cssNo = 1;
        }
        $file_name = DOCUMENTROOT."/common/gkcss/gk".$cssNo.$size.".css";
        //ファイルの更新日時取得
        $date = View::echo_filedate($file_name);

        $args["CSS_FILE"] = REQUESTROOT."/common/gkcss/gk".$cssNo.$size.".css?date=".$date;;

        HtmlTemplate::t_include(TMPLDIRECTORY ."/main5_Jquery11.html",$args);
    }

    //グラフ表示用（gk.cssを使う、css/jsの複数ファイル対応）
    function toHTML6(&$model, $file, $data, $jsplugin="", $cssplugin="")
    {
        //Onlineユーザー数
        if(!file_exists(USR_LST)){
            $nf = fopen(USR_LST, "w");
            fclose($nf);
        }

        //20160427jsファイルの更新日時も取得
        $jsfilename = str_replace(REQUESTROOT,"",COMMON_JS_FILE);
        $commonjs = DOCUMENTROOT.$jsfilename;
        $jsdate = View::echo_filedate($commonjs);

        //Javascriptファイル名
        $jsFile = str_replace(".html", ".js", basename($file));
        //リストの幅
        if (!isset($data["SEL_WIDTH"])) $data["SEL_WIDTH"] = "60%";
        $args = array("BODY"                =>HtmlTemplate::t_buffer($file, $data),
                      "HEADER"              =>(isset($data["HEADER"]))? $data["HEADER"] : '',
                      "JAVASCRIPT"          =>((isset($data["JAVASCRIPT"]))? $data["JAVASCRIPT"] : '') .View::jsMessage($jsFile),
                      "COMMON_JS_FILE"      =>COMMON_JS_FILE."?date=".$jsdate,
                      "COMMON_JS_PROTO"         =>COMMON_JS_PROTO,
                      "COMMON_JS_EFFECT"        =>COMMON_JS_EFFECT,
                      "COMMON_JS_SMARTDIALOG"   =>COMMON_JS_SMARTDIALOG,
                      //"COMMON_JS_FILE"      =>COMMON_JS_FILE,
                      //"OTHER_JS_FILE"       =>REQUESTROOT."/common/jquery.js",
                      //"OTHER_JS_FILE2"      =>REQUESTROOT."/common/boxover.js",
                      //"CSS_FILE"            =>CSS_FILE,//デフォルトでも使用しない
                      "TITLE"               =>(isset($data["TITLE"]))? $data["TITLE"] : TITLE,
                      "CHARSET"             =>CHARSET,
                      "AUTHORITY"           =>AUTHORITY,
                      "PROGRAMID"           =>PROGRAMID,
                      "COPYRIGHT"           =>COPYRIGHT,
                      "ERROR_MESSAGE"       => View::showAlert($model)
                      );
        //DBからgk.cssの種類を変更したい
        $db = Query::dbCheckOut();
        $staffQuery = " SELECT FIELD1, FIELD2 FROM STAFF_DETAIL_SEQ_MST WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
        $staffRow = $db->getRow($staffQuery, DB_FETCHMODE_ASSOC);

        $cssNo = $staffRow["FIELD1"];

        if($staffRow["FIELD2"] != "2"){
            $size = "";
        }else{
            $size = "big";
        }

        if($cssNo == ""){
            $cssNo = 1;
        }
        $file_name = DOCUMENTROOT."/common/gkcss/gk".$cssNo.$size.".css";
        //ファイルの更新日時取得
        $date = View::echo_filedate($file_name);

        $args["CSS_FILE"] = REQUESTROOT."/common/gkcss/gk".$cssNo.$size.".css?date=".$date;;
        Query::dbCheckIn($db);

       //*20100728 jspluginを配列化*
        if($jsplugin!=""){
           if(strstr($jsplugin,"|")){
              $jsplugin_row    = explode("|",$jsplugin);
          }else{
              $jsplugin_row[0] = $jsplugin;
          }
         $i=0;
         foreach($jsplugin_row as $key => $val)
         {
          $file_name = DOCUMENTROOT."/common/js/".$val;
          //ファイルの更新日時取得
          $date = View::echo_filedate($file_name);

          //$args["OTHER_JS_FILE".$i] = REQUESTROOT."/X/KNJXMENU/js/".$val;
          $args["OTHER_JS_FILE".$i] = REQUESTROOT."/common/js/".$val."?date=".$date;
          $i++;
          if($i==10){break;}
         }
        }
       //*20100728 csspluginを配列化*
        if($cssplugin!=""){
           if(strstr($cssplugin,"|")){
              $cssplugin_row    = explode("|",$cssplugin);
          }else{
              $cssplugin_row[0] = $cssplugin;
          }
         $i=0;
         foreach($cssplugin_row as $key => $val)
         {
          $file_name = DOCUMENTROOT."/common/kenja-design-sample/".$val;
          //ファイルの更新日時取得
          $date = View::echo_filedate($file_name);

          //$args["OTHER_CSS_FILE".$i] = REQUESTROOT."/X/KNJXMENU/kenja-design-sample/".$val;
          $args["OTHER_CSS_FILE".$i] = REQUESTROOT."/common/kenja-design-sample/".$val."?date=".$date;
          $i++;
          if($i==10){break;}
         }
        }

       // echo "@@@@args@@@@<br>";
       // new dBug($args);


        HtmlTemplate::t_include(TMPLDIRECTORY ."/main_test3.html",$args);
    }

    //MAIN共通
    function toHTML7(&$model, $file, $data)
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
        $args = array("BODY"                    =>HtmlTemplate::t_buffer($file, $data),
                      "HEADER"                  =>(isset($data["HEADER"]))? $data["HEADER"] : '',
                      "JAVASCRIPT"              =>((isset($data["JAVASCRIPT"]))? $data["JAVASCRIPT"] : '') .View::jsMessage($jsFile),
                      "COMMON_JS_FILE"          =>COMMON_JS_FILE,
                      "COMMON_TEXTAREAJS_FILE"  =>COMMON_TEXTAREAJS_FILE,
                      "COMMON_TABLE_SORT"       =>COMMON_TABLE_SORT,
                      "COMMON_JS_PROTO"         =>COMMON_JS_PROTO,
                      "COMMON_JS_EFFECT"        =>COMMON_JS_EFFECT,
                      "COMMON_JS_SMARTDIALOG"   =>COMMON_JS_SMARTDIALOG,
                      "CSS_FILE"                =>CSS_FILE,
                      "JQUERY"                  =>JQUERY,
                      "D_AND_D"                 =>D_AND_D,
                      "D_AND_D_HEAD"            =>D_AND_D_HEAD,
                      "TITLE"                   =>(isset($data["TITLE"]))? $data["TITLE"] : TITLE,
                      "CHARSET"                 =>CHARSET,
                      "AUTHORITY"               =>AUTHORITY,
                      "PROGRAMID"               =>PROGRAMID,
                      "COPYRIGHT"               =>COPYRIGHT,
                      "ERROR_MESSAGE"           => View::showAlert($model)
                      );

        HtmlTemplate::t_include(TMPLDIRECTORY ."/main5_Jquery12.html",$args);
    }

    //css/jsファイルの更新日付を取得
    function echo_filedate($filename){
        if(file_exists($filename)){
            return date('YmdHis', filemtime($filename));
        }else{
            echo "file not found<BR>";
        }
    }

    //MAIN共通
    function toHTMLChart(&$model, $file, $data)
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
                      "COMMON_TABLE_SORT"   =>COMMON_TABLE_SORT,
                      //"CSS_FILE"            =>CSS_FILE,
                      "CCCHART"             =>REQUESTROOT."/common/js/ccchart.js",
                      "CHART"               =>REQUESTROOT."/common/js/Chart.js",
                      "TITLE"               =>(isset($data["TITLE"]))? $data["TITLE"] : TITLE,
                      "CHARSET"             =>CHARSET,
                      "AUTHORITY"           =>AUTHORITY,
                      "PROGRAMID"           =>PROGRAMID,
                      "COPYRIGHT"           =>COPYRIGHT,
                      "ERROR_MESSAGE"       => View::showAlert($model)
                      );
        //DBからgk.cssの種類を変更したい
        $db = Query::dbCheckOut();
        $staffQuery = " SELECT FIELD1, FIELD2 FROM STAFF_DETAIL_SEQ_MST WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
        $staffRow = $db->getRow($staffQuery, DB_FETCHMODE_ASSOC);

        $cssNo = $staffRow["FIELD1"];

        if($staffRow["FIELD2"] != "2"){
            $size = "";
        }else{
            $size = "big";
        }

        if($cssNo == ""){
            $cssNo = 1;
        }
        $file_name = DOCUMENTROOT."/common/gkcss/gk".$cssNo.$size.".css";
        //ファイルの更新日時取得
        $date = View::echo_filedate($file_name);

        $args["CSS_FILE"] = REQUESTROOT."/common/gkcss/gk".$cssNo.$size.".css?date=".$date;;
        Query::dbCheckIn($db);

        HtmlTemplate::t_include(TMPLDIRECTORY ."/mainchart.html",$args);
    }

    //MAIN共通
    function toHTMLChart2(&$model, $file, $data)
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
                      "COMMON_TABLE_SORT"   =>COMMON_TABLE_SORT,
                      "CSS_FILE2"           =>REQUESTROOT."/common/js/morris.css",
                      "JQUERY"              =>REQUESTROOT."/common/js/jquery-1.11.0.min.js",
                      "CCCHART"             =>REQUESTROOT."/common/js/ccchart.js",
                      "CHART"               =>REQUESTROOT."/common/js/Chart.js",
                      "RAPHAEL"             =>REQUESTROOT."/common/js/raphael-min.js",
                      "MORRIS"              =>REQUESTROOT."/common/js/morris.min.js",
                      "TITLE"               =>(isset($data["TITLE"]))? $data["TITLE"] : TITLE,
                      "CHARSET"             =>CHARSET,
                      "AUTHORITY"           =>AUTHORITY,
                      "PROGRAMID"           =>PROGRAMID,
                      "COPYRIGHT"           =>COPYRIGHT,
                      "ERROR_MESSAGE"       => View::showAlert($model)
                      );
        //DBからgk.cssの種類を変更したい
        $db = Query::dbCheckOut();
        $staffQuery = " SELECT FIELD1, FIELD2 FROM STAFF_DETAIL_SEQ_MST WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
        $staffRow = $db->getRow($staffQuery, DB_FETCHMODE_ASSOC);

        $cssNo = $staffRow["FIELD1"];

        if($staffRow["FIELD2"] != "2"){
            $size = "";
        }else{
            $size = "big";
        }

        if($cssNo == ""){
            $cssNo = 1;
        }
        $file_name = DOCUMENTROOT."/common/gkcss/gk".$cssNo.$size.".css";
        //ファイルの更新日時取得
        $date = View::echo_filedate($file_name);

        $args["CSS_FILE"] = REQUESTROOT."/common/gkcss/gk".$cssNo.$size.".css?date=".$date;;
        Query::dbCheckIn($db);

        HtmlTemplate::t_include(TMPLDIRECTORY ."/mainchart2.html",$args);
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
                        $msg = str_replace("@CTRL_SEMESTERNAME", CTRL_SEMESTERNAME, $msg);
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
    //iフレーム作成
    function iframe($args, $file='iframe1.html',$border=1)
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
    // Edit by PP for PC-Talker end 2020/02/03
    function popUpCalendar(&$form, $name, $value="",$param="", $textLabel="")
    // Edit by PP for PC-Talker end 2020/02/20
    {

        //DBからgk.cssの種類を変更したい
        $db = Query::dbCheckOut();
        $staffQuery = " SELECT FIELD1, FIELD2 FROM STAFF_DETAIL_SEQ_MST WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
        $staffRow = $db->getRow($staffQuery, DB_FETCHMODE_ASSOC);

        $cssNo = $staffRow["FIELD1"];

        if($staffRow["FIELD2"] != "2"){
            $size = "";
        }else{
            $size = "big";
        }

        if($cssNo == ""){
            $cssNo = 1;
        }

        global $sess;
        //テキストエリア
        // Edit by PP for PC-Talker end 2020/02/03
        $label = ($textLabel=="")?"aria-label='作成年月日'":"aria-label='$textLabel'";
        // Edit by PP for PC-Talker end 2020/02/20
        $form->ae( array("type"        => "text",
                        "name"        => $name,
                        "size"        => 12,
                        "maxlength"   => 12,
                        // Edit by PP for PC-Talker end 2020/02/03
                        "extrahtml"   => "onblur=\"isDate(this)\" $label id=\"$name\"",
                        // Edit by PP for PC-Talker end 2020/02/20
                        "value"       => $value));

        //読込ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_calen",
                        "value"       => "･･･",
                        // Edit by PP for PC-Talker end 2020/02/03
                        "extrahtml"   => "aria-label=\"カレンダー\" id=\"{$name}_btn\" onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value + '&CAL_SESSID=$sess->id&$param' + '&CSSNO=$cssNo', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;scrollY = window.innerHeight > event.clientY + 200 ? scrollY : scrollY - 200;return scrollY;}(), 360, 200)\"") );
                        // Edit by PP for PC-Talker end 2020/02/20

        return View::setIframeJs() .$form->ge($name) .$form->ge("btn_calen");
    }

    //カレンダーコントロール
    function popUpCalendar2(&$form, $name, $value="",$param="",$extra="",$disabled="")
    {

        //DBからgk.cssの種類を変更したい
        $db = Query::dbCheckOut();
        $staffQuery = " SELECT FIELD1, FIELD2 FROM STAFF_DETAIL_SEQ_MST WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
        $staffRow = $db->getRow($staffQuery, DB_FETCHMODE_ASSOC);

        $cssNo = $staffRow["FIELD1"];

        if($staffRow["FIELD2"] != "2"){
            $size = "";
        }else{
            $size = "big";
        }

        if($cssNo == ""){
            $cssNo = 1;
        }

        global $sess;
        //テキストエリア
        $form->ae( array("type"        => "text",
                        "name"        => $name,
                        "size"        => 12,
                        "maxlength"   => 12,
                        "extrahtml"   => "onblur=\"isDate(this);$extra\"".$disabled,
                        "value"       => $value));

        //読込ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_calen",
                        "value"       => "･･･",
                        "extrahtml"   => "onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value + '&CAL_SESSID=$sess->id&$param' + '&CSSNO=$cssNo', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;scrollY = window.innerHeight > event.clientY + 200 ? scrollY : scrollY - 200;return scrollY;}(), 320, 200)\"") );

        return View::setIframeJs() .$form->ge($name) .$form->ge("btn_calen");
    }

    //カレンダーコントロール
    function popUpCalendarAlp(&$form, $name, $value, $disabled, $param = "")
    {

        //20160428DBからgk.cssの種類を変更したい
        $db = Query::dbCheckOut();
        $staffQuery = " SELECT FIELD1, FIELD2 FROM STAFF_DETAIL_SEQ_MST WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
        $staffRow = $db->getRow($staffQuery, DB_FETCHMODE_ASSOC);

        $cssNo = $staffRow["FIELD1"];

        if($staffRow["FIELD2"] != "2"){
            $size = "";
        }else{
            $size = "big";
        }

        if($cssNo == ""){
            $cssNo = 1;
        }

        global $sess;
        //テキストエリア
        $form->ae( array("type"        => "text",
                        "name"        => $name,
                        "size"        => 12,
                        "maxlength"   => 12,
//                        "extrahtml"   => $disabled." onblur=\"isDate(this);\"",
                        "extrahtml"   => "onblur=\"isDate(this);$extra\"".$disabled,
                        "value"       => $value));

        //読込ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_calen",
                        "value"       => "･･･",
                        "extrahtml"   => $disabled." onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value + '&CAL_SESSID=$sess->id&$param' + '&CSSNO=$cssNo', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;scrollY = window.innerHeight > event.clientY + 200 ? scrollY : scrollY - 200;return scrollY;}(), 320, 200)\"") );

        return View::setIframeJs() .$form->ge($name) .$form->ge("btn_calen");
    }

    //カレンダーコントロール
    function popUpCalendarAlp2(&$form, $name, $value, $disabled, $param = "")
    {
        global $sess;
        //テキストエリア
        $form->ae( array("type"        => "text",
                        "name"        => $name,
                        "size"        => 12,
                        "maxlength"   => 12,
//                        "extrahtml"   => $disabled." onblur=\"isDate(this);\"",
                        "extrahtml"   => "onblur=\"isDate(this);$extra\"".$disabled,
                        "value"       => $value));

        //読込ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_calen",
                        "value"       => "･･･",
                        "extrahtml"   => $disabled." onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;scrollY = window.innerHeight > event.clientY + 200 ? scrollY : scrollY - 200;return scrollY;}(), 320, 200)\"") );

        return $form->ge($name) .$form->ge("btn_calen");
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
    function popUpZipCode(&$form, $zipname, $value="", $name="", $size=10, $name2="")
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
                        "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=$name&addr2name={$name2}&zipname=$zipname&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;scrollY = window.innerHeight > event.clientY + 200 ? scrollY : scrollY - 200;return scrollY;}(), 320, 260)\"") );

        //確定ボタンを作成する
        $form->ae( array("type" => "button",
                        "name"        => "btn_apply",
                        "value"       => "確定",
                        "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=$name&addr2name={$name2}&zipname=$zipname&zip='+document.forms[0]['$zipname'].value+'&frame='+getFrameName(self))\"") );

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
/* Edit by Kaung for current_cursor start 2020/01/20 */
    function updateNext(&$model, &$objForm, $btn='btn_update', $current = ''){
	    if($current == ''){
	        //更新ボタン
            $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" onclick=\"document.forms[0].btn_up_next.disabled = true;this.disabled = true;top.main_frame.left_frame.updateNext(self, 'pre','".$btn ."');\""));

            //更新ボタン
            $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" onclick=\"document.forms[0].btn_up_pre.disabled = true;this.disabled = true;top.main_frame.left_frame.updateNext(self, 'next','".$btn ."');\""));
	    } else {
	        //更新ボタン
            $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の生徒へ",
                            "extrahtml" =>  "id=\"btn_up_pre\" style=\"width:130px\" onclick=\"".$current."('btn_up_pre');top.main_frame.left_frame.updateNext(self, 'pre','".$btn ."');\""));
                            //  document.forms[0].btn_up_next.disabled = true;this.disabled = true;

            //更新ボタン
            $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の生徒へ",
                            "extrahtml" =>  "id=\"btn_up_next\" style=\"width:130px\" onclick=\"".$current."('btn_up_next'); top.main_frame.left_frame.updateNext(self, 'next','".$btn ."');\""));
                            // document.forms[0].btn_up_pre.disabled = true;this.disabled = true;
	    }
        
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
    /* Edit by Kaung for current_cursor start 2020/01/31 */
/*
    更新後前の生徒へボタン作成
    引数１：モデルオブジェクト変数
    引数２：フォームオブジェクト変数
    引数３：更新時のボタン名
*/
/* Edit by HPA for current_cursor start 2020/02/03 */
    function updateNext2(&$model, &$objForm, $schregno, $schregnoField, $repCmd, $setUpdCmd, $pid = '' ){
      if($pid == ''){
        //更新ボタン
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" onclick=\"updateNextStudent2('".$schregno."', 1, '".$schregnoField."', '".$repCmd."', '".$setUpdCmd."');\" style=\"width:130px\""));

        //更新ボタン
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" onclick=\"updateNextStudent2('".$schregno."', 0, '".$schregnoField."', '".$repCmd."', '".$setUpdCmd."');\" style=\"width:130px\""));

        return $objForm->ge("btn_up_pre") .$objForm->ge("btn_up_next");

      } else {
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" id=\"btn_up_pre\" onclick=\"current_cursor('btn_up_pre');updateNextStudent2('".$schregno."', 1, '".$schregnoField."', '".$repCmd."', '".$setUpdCmd."');\" style=\"width:130px\""));

        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の生徒へ",
                            "extrahtml" =>  "style=\"width:130px\" id=\"btn_up_next\" onclick=\"current_cursor('btn_up_next');updateNextStudent2('".$schregno."', 0, '".$schregnoField."', '".$repCmd."', '".$setUpdCmd."');\" style=\"width:130px\""));

        return $objForm->ge("btn_up_pre") .$objForm->ge("btn_up_next");
      }
    }
    /* Edit by HPA for current_cursor end 2020/02/20 */

    /*
     * popUpSchoolCd
     * &$objForm
     * $textName        学校CDテキストの名前
     * $textVal         学校CDテキストの値 初期値
     * $schoolCdVal     親画面の学校CDテキストの値を取得 例："document.forms[0]['J_SCHOOL_CD'].value"
     * $kensakuName     学校検索ボタンの名前
     * $kakuteiName     確定ボタンの名前
     * $fscdname        検索結果の学校CDをセットするフィールド名
     * $fsname          検索結果の学校名をセットするフィールド名
     * $fsChikuName     検索結果の地区名をセットするフィールド名
     * $fsRitsuNameId   検索結果の〇立をセットするフィールド名
     * $fsaddr          検索結果の学校住所をセットするフィールド名
     * $school_div      検索結果の学校種類をセットするフィールド名
     * $setschooltype   検索時に幼稚園、小学校、中学校、高校を指定する値(名称マスタL019に対応)
     * 例：
     *   //学校検索共通
     *   $schoolCdVal = "document.forms[0]['J_SCHOOL_CD'].value";
     *   $arg["data"]["J_SCHOOL_CD"] = View::popUpSchoolCd(&$objForm, "J_SCHOOL_CD", $Row["J_SCHOOL_CD"], $schoolCdVal, "btn_kensaku", "btn_kakutei", "J_SCHOOL_CD", "J_SCHOOL_NAME", "", "J_SCHOOL_RITSU", "", "", "3", "");
     */
    // Edit by PP for current_cursor start 2020/02/03
    function popUpSchoolCd(&$objForm, $textName, $textVal, $schoolCdVal, $kensakuName, $kakuteiName, $fscdname, $fsname, $fsChikuName, $fsRitsuNameId, $fsaddr, $school_div, $setschooltype, $textExtra = "", $btnExtra = "")
    {
        //学校CD
        $extra = $textExtra;
        $schoolCdText = knjCreateTextBox($objForm, $textVal, $textName, 7, 7, $extra);
        //検索
        $extra = "id=\"$kensakuName\" $btnExtra style=\"width:80px\" onclick=\"current_cursor('$kensakuName'); loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&fscdname=$fscdname&fsname=$fsname&fsChikuName=$fsChikuName&fsRitsuNameId=$fsRitsuNameId&fsaddr=$fsaddr&school_div=$school_div&setschooltype=$setschooltype', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;scrollY = window.innerHeight > event.clientY + 200 ? scrollY : scrollY - 200;return scrollY;}(), 500, 320)\"";
        $kensaku = knjCreateBtn($objForm, $kensakuName, "学校検索", $extra);

        return  View::setIframeJs() .$schoolCdText .$kensaku ;
    }

}
?>
