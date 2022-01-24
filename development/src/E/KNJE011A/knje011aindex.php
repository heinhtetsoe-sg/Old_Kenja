<?php

require_once('for_php7.php');

require_once('knje011aModel.inc');
require_once('knje011aQuery.inc');

class knje011aController extends Controller {
    var $ModelClassName = "knje011aModel";
    var $ProgramID      = "KNJE011A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "reload3":     //RECORD_TOTALSTUDYTIME_DAT (通知書) より読込む
                case "updEdit":
                case "edit":
                case "yomikomi":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje011aForm1");
                    break 2;
                case "reload2_ok":  //学習指導要録より読込(OK)
                case "reload2_cancel":  //学習指導要録より読込(キャンセル)
                case "reload4":     //通知書より読込
                case "torikomi3":
                case "torikomi4":
                case "form2_first": //「出欠の～」の最初の呼出
                case "form2":       //出欠の～
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje011aForm2");
                    break 2;
                case "form3_first": //「成績参照」の最初の呼出
                case "form3":       //「成績参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje011aSubForm1");
                    break 2;
                case "form4_first": //「指導要録参照」の最初の呼出
                case "form4":       //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje011aSubForm2");
                    break 2;
                case "form6_first": //「指導要録参照」の最初の呼出
                case "form6":       //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knje011aSubForm6");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011aForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011aForm2");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "reload":      //保健より読み込み
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->getReloadHealthModel();
                    break 2;
                case "replace":
                    $this->callView("knje011aSubForm3"); //一括更新画面
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->ReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "reset":
                    $this->callView("knje011aForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE011A/knje011aindex.php?cmd=edit") ."&button=3";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje011aindex.php?cmd=edit&init=1";
                    $args["cols"] = "23%,*";
                    View::frame($args);
                    exit;
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE011A/knje011aindex.php?cmd=edit") ."&button=3";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&GRADE={$sessionInstance->gradeHrClass}";
                    $args["right_src"] = "knje011aindex.php?cmd=edit";
                    $args["cols"] = "23%,*";
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
$knje011aCtl = new knje011aController;
//var_dump($_REQUEST);
?>
