<?php
require_once('knja123ooModel.inc');
require_once('knja123ooQuery.inc');

class knja123ooController extends Controller {
    var $ModelClassName = "knja123ooModel";
    var $ProgramID      = "KNJA123OO";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knja123ooForm1");
                    break 2;
                case "sslApplet":
                    $sessionInstance->knja123ooModel();
                    $this->callView("knja123ooForm1");
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
                    $this->callView("knja123ooForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->getSendAuth();
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&SEND_AUTH=".$sessionInstance->sendAuth."&PATH=" .urlencode("/A/KNJA123OO/knja123ooindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knja123ooindex.php?cmd=edit";
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
$knja123ooCtl = new knja123ooController;
?>
