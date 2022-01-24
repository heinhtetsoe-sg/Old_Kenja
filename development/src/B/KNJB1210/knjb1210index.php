<?php

require_once('for_php7.php');

require_once('knjb1210Model.inc');
require_once('knjb1210Query.inc');

class knjb1210Controller extends Controller {
    var $ModelClassName = "knjb1210Model";
    var $ProgramID      = "KNJB1210";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "next":
                case "prev":
                case "pattern":
                case "select":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjb1210Form1");
                   break 2;
                case "update":
                case "updateNext":
                case "updatePrev":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP4/index.php?";
                    $args["left_src"] .= "PROGRAMID=".$this->ProgramID;
                    $args["left_src"] .= "&AUTH=".AUTHORITY;
                    $args["left_src"] .= "&search_div=1";
                    $args["left_src"] .= "&name=1";
                    $args["left_src"] .= "&name_kana=1";
                    $args["left_src"] .= "&ent_year=1";
                    $args["left_src"] .= "&grd_year=1";
                    $args["left_src"] .= "&schno=1";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&PATH=" .urlencode("/B/KNJB1210/knjb1210index.php?cmd=main");
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}";
                    $args["right_src"] = "knjb1210index.php?cmd=main";
                    $args["cols"] = "25%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1210Ctl = new knjb1210Controller;
?>
