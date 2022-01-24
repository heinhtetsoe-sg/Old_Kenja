<?php

require_once('for_php7.php');
require_once('knja121bModel.inc');
require_once('knja121bQuery.inc');

class knja121bController extends Controller {
    var $ModelClassName = "knja121bModel";
    var $ProgramID      = "KNJA121B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja121bForm1");
                    break 2;
                case "tuutihyou":   //通知票所見参照
                    $this->callView("tuutihyou");
                    break 2;
                case "koudou":      //行動の記録備考
                    $this->callView("koudou");
                    break 2;
                case "koudou_update":       //行動の記録備考の更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel_koudou();
                    $sessionInstance->setCmd("koudou");
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
                    $this->callView("knja121bForm");
                    break 2;
                case "main":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA121B/knja121bindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knja121bindex.php?cmd=edit";
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
$knja121bCtl = new knja121bController;
?>
