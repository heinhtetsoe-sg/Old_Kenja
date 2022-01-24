<?php

require_once('for_php7.php');

require_once('knje010bModel.inc');
require_once('knje010bQuery.inc');

class knje010bController extends Controller {
    var $ModelClassName = "knje010bModel";
    var $ProgramID      = "KNJE010B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "reload2_ok":
                case "reload2_cancel":
                case "reload3":
                case "reload3_1":
                case "reload3_2":
                case "reload3_3":
                case "yomikomi":
                case "torikomi0":
                case "torikomi1":
                case "torikomi2":
                case "torikomiT0":
                case "torikomiT1":
                case "torikomiT2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje010bForm1");
                    break 2;
                case "form3_first": //「成績参照」の最初の呼出
                case "form3": //「成績参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje010bSubForm1");
                    break 2;
                case "form4_first": //「指導要録参照」の最初の呼出
                case "form4": //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje010bSubForm2");
                    break 2;
                case "subform6": //旧調査書の指導上参照となる諸事項
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje010bSubForm6");
                    break 2;
                case "form7_first": //「指導要録参照」の最初の呼出
                case "form7": //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje010bSubForm7");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje010bForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "reload":  //保健より読み込み
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->getReloadHealthModel();
                    break 2;
                case "reset":
                    $this->callView("knje010bForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE010B/knje010bindex.php?cmd=edit") ."&button=3";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje010bindex.php?cmd=edit&init=1";
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
$knje010bCtl = new knje010bController;
//var_dump($_REQUEST);
?>
