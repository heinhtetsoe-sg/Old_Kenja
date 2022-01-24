<?php

require_once('for_php7.php');
require_once('knjm260aModel.inc');
require_once('knjm260aQuery.inc');

class knjm260aController extends Controller {
    var $ModelClassName = "knjm260aModel";
    var $ProgramID      = "KNJM260A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "reset":
                case "main":
                    $this->callView("knjm260aForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP4/index.php?";
                    $args["left_src"] .= "PROGRAMID=".$this->ProgramID;
                    $args["left_src"] .= "&AUTH=".AUTHORITY;
                    $args["left_src"] .= "&hr_class=1";
                    $args["left_src"] .= "&name=1";
                    $args["left_src"] .= "&name_kana=1";
                    $args["left_src"] .= "&schno=1";
                    $args["left_src"] .= "&sort=1";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}";
                    $args["left_src"] .= "&PATH=" .urlencode("/M/KNJM260A/knjm260aindex.php?cmd=main");
                    $args["right_src"] = "knjm260aindex.php?cmd=main";

                    $args["cols"] = "20%,*";
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
$knjm260aCtl = new knjm260aController;
?>
