<?php

require_once('for_php7.php');
require_once('knje450Model.inc');
require_once('knje450Query.inc');

class knje450Controller extends Controller {
    var $ModelClassName = "knje450Model";
    var $ProgramID      = "KNJE450";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje450Form1");
                    break 2;
                case "subform1":
                case "subform1A":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje450SubForm1");
                    break 2;
                case "subform2":
                case "subform2A":
                case "subform2_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje450SubForm2");
                    break 2;
                //障害マスタ参照
                case "reference1":
                    $this->callView("knje450SubRef1");
                    break 2;
                case "subform1_update":
                case "subform1_insert":
                case "subform2_update":
                case "subform2_insert":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "":
                    //分割フレーム作成
                    if ($sessionInstance->Properties["KNJE450_useKNJXEXP_GUI"] == "1") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GUI/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/E/KNJE450/knje450index.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/E/KNJE450/knje450index.php?cmd=edit") ."&button=noUse" ."&SES_FLG=2";
                    }
                    $args["right_src"] = "knje450index.php?cmd=edit";
                    $args["cols"] = "22%,*";
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
$knje450Ctl = new knje450Controller;
?>
