<?php

require_once('for_php7.php');

require_once('knja120bModel.inc');
require_once('knja120bQuery.inc');

class knja120bController extends Controller {
    var $ModelClassName = "knja120bModel";
    var $ProgramID      = "KNJA120B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "torikomi4":
                case "reload"://通知票、調査書取込
                case "reload2"://3年次通知票取込
                case "edit":
                case "clear":
                    $this->callView("knja120bForm1");
                    break 2;
                case "tuutihyou":
                    $this->callView("tuutihyou");
                    break 2;
                case "syukketu":
                    $this->callView("syukketu");
                    break 2;
                case "tyousasyo":
                    $this->callView("tyousasyo");
                    break 2;
                case "shokenlist1":
                case "shokenlist2":
                case "shokenlist3":
                    $this->callView("shokenlist");
                    break 2;
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knja120bForm");
                    break 2;
                case "main":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/A/KNJA120B/knja120bindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knja120bindex.php?cmd=edit";
                    $args["cols"] = "25%,75%";
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
$knja120bCtl = new knja120bController;
?>
