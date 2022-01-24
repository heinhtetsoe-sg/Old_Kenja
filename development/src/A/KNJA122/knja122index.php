<?php

require_once('for_php7.php');

require_once('knja122Model.inc');
require_once('knja122Query.inc');

class knja122Controller extends Controller {
    var $ModelClassName = "knja122Model";
    var $ProgramID      = "KNJA122";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja122Form1");
                    break 2;
                case "subform1": //行動の記録
                case "clear1": //行動の記録
                    $this->callView("knja122SubForm1");
                    break 2;
                case "subform2": //特別活動の記録
                case "clear2": //特別活動の記録
                    $this->callView("knja122SubForm2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update1":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel1();
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel2();
                    $sessionInstance->setCmd("subform2");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/A/KNJA122/knja122index.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knja122index.php?cmd=edit";
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
$knja122Ctl = new knja122Controller;
?>
