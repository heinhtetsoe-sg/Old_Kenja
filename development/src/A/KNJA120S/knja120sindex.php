<?php

require_once('for_php7.php');
require_once('knja120sModel.inc');
require_once('knja120sQuery.inc');

class knja120sController extends Controller {
    var $ModelClassName = "knja120sModel";
    var $ProgramID      = "KNJA120S";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knja120sForm1");
                    break 2;
                case "sslApplet":
                    $sessionInstance->knja120sModel();
                    $this->callView("knja120sForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getShomeiModel();
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
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->getSendAuth();
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&SEND_AUTH=".$sessionInstance->sendAuth."&PATH=" .urlencode("/A/KNJA120S/knja120sindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knja120sindex.php?cmd=edit";
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
$knja120sCtl = new knja120sController;
?>
