<?php

require_once('for_php7.php');
require_once('knjd137fModel.inc');
require_once('knjd137fQuery.inc');

class knjd137fController extends Controller {
    var $ModelClassName = "knjd137fModel";
    var $ProgramID      = "KNJD137F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                    $this->callView("knjd137fForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "subform1":    //部活動参照
                    $this->callView("knjd137fSubForm1");
                    break 2;
                case "subform2":    //委員会参照
                    $this->callView("knjd137fSubForm2");
                    break 2;
                case "subform3":    //資格参照
                    $this->callView("knjd137fSubForm3");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD137F/knjd137findex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjd137findex.php?cmd=edit";
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
$knjd137fCtl = new knjd137fController;
?>
