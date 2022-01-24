<?php

require_once('for_php7.php');
require_once('knja127aModel.inc');
require_once('knja127aQuery.inc');

class knja127aController extends Controller {
    var $ModelClassName = "knja127aModel";
    var $ProgramID      = "KNJA127A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "torikomi3":
                case "chousasho":
                case "edit":
                case "clear":
                    $this->callView("knja127aForm1");
                    break 2;
                case "subform1": //障害の状態
                    $this->callView("knja127aSubForm1");
                    break 2;
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
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == "") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&special_div=1AND2AND3&schoolKind=A&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA127A/knja127aindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&special_div=1AND2AND3&schoolKind=A&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode($programpath."/knja127aindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["right_src"] = "knja127aindex.php?cmd=edit";
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
$knja127aCtl = new knja127aController;
?>
