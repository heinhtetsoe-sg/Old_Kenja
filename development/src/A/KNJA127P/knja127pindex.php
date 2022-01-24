<?php

require_once('for_php7.php');

require_once('knja127pModel.inc');
require_once('knja127pQuery.inc');

class knja127pController extends Controller {
    var $ModelClassName = "knja127pModel";
    var $ProgramID      = "KNJA127P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja127pForm1");
                    break 2;
                case "subform1":
                    $this->callView("knja127pSubForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "teikei":
                    $this->callView("knja127pSubMaster");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&special_div=1AND2AND3&schoolKind=P&TARGET=right_frame&PATH=" .urlencode("/A/KNJA127P/knja127pindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1";
                    $args["right_src"] = "knja127pindex.php?cmd=edit";
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
$knja127pCtl = new knja127pController;
?>
