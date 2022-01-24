<?php

require_once('for_php7.php');
require_once('knja123psModel.inc');
require_once('knja123psQuery.inc');

class knja123psController extends Controller {
    var $ModelClassName = "knja123psModel";
    var $ProgramID      = "KNJA123PS";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knja123psForm1");
                    break 2;
                case "sslApplet":
                    $sessionInstance->knja123psModel();
                    $this->callView("knja123psForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getCancelModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "shomei":
                    $sessionInstance->getShomeiModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "sasimodosi":
                    $sessionInstance->getSasimodosiModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "form2":
                    $this->callView("knja123psForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->getSendAuth();
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&SEND_AUTH=".$sessionInstance->sendAuth."&PATH=" .urlencode("/A/KNJA123PS/knja123psindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knja123psindex.php?cmd=edit";
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
$knja123psCtl = new knja123psController;
?>
