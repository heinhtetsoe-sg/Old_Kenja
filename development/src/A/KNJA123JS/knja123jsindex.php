<?php

require_once('for_php7.php');
require_once('knja123jsModel.inc');
require_once('knja123jsQuery.inc');

class knja123jsController extends Controller {
    var $ModelClassName = "knja123jsModel";
    var $ProgramID      = "KNJA123JS";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knja123jsForm1");
                    break 2;
                case "sslApplet":
                    $sessionInstance->knja123jsModel();
                    $this->callView("knja123jsForm1");
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
                    $this->callView("knja123jsForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->getSendAuth();
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&SEND_AUTH=".$sessionInstance->sendAuth."&PATH=" .urlencode("/A/KNJA123JS/knja123jsindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knja123jsindex.php?cmd=edit";
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
$knja123jsCtl = new knja123jsController;
?>
