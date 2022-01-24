<?php

require_once('for_php7.php');

require_once('knje012qModel.inc');
require_once('knje012qQuery.inc');

class knje012qController extends Controller {
    var $ModelClassName = "knje012qModel";
    var $ProgramID      = "KNJE012Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "torikomi0":
                case "torikomi1":
                case "torikomi2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje012qForm1");
                    break 2;
                case "syukketsuKirokuSansyo": //出欠の記録参照
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("SyukketsuKirokuSansyo");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje012qForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "reset":
                    $this->callView("knje012qForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE012Q/knje012qindex.php?cmd=edit") ."&button=3";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje012qindex.php?cmd=edit&init=1";
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
$knje012qCtl = new knje012qController;
//var_dump($_REQUEST);
?>
