<?php

require_once('for_php7.php');
require_once('knjd137gModel.inc');
require_once('knjd137gQuery.inc');

class knjd137gController extends Controller {
    var $ModelClassName = "knjd137gModel";
    var $ProgramID      = "KNJD137G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                    $this->callView("knjd137gForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "subform1":    //部活動参照
                    $this->callView("knjd137gSubForm1");
                    break 2;
                case "subform2":    //委員会参照
                    $this->callView("knjd137gSubForm2");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD137G/knjd137gindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjd137gindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
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
$knjd137gCtl = new knjd137gController;
?>
