<?php

require_once('for_php7.php');


require_once('knjb1230Model.inc');
require_once('knjb1230Query.inc');

class knjb1230Controller extends Controller {
    var $ModelClassName = "knjb1230Model";
    var $ProgramID      = "KNJB1230";

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
                    $this->callView("knjb1230Form1");
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
                    $args["left_src"] .= "&search_div2=1";
                    $args["left_src"] .= "&name=1";
                    $args["left_src"] .= "&name_kana=1";
                    $args["left_src"] .= "&hr_class=1";
                    $args["left_src"] .= "&ent_year=1";
                    $args["left_src"] .= "&grd_year=1";
                    $args["left_src"] .= "&course_major=1";
                    $args["left_src"] .= "&coursecode=1";
                    $args["left_src"] .= "&schno=1";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}";
                    $args["left_src"] .= "&PATH=" .urlencode("/B/KNJB1230/knjb1230index.php?cmd=main");
                    $args["right_src"] = "knjb1230index.php?cmd=main";
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
$knjb1230Ctl = new knjb1230Controller;
?>
