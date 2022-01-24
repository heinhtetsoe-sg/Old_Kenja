<?php

require_once('for_php7.php');

require_once('knje010cModel.inc');
require_once('knje010cQuery.inc');

class knje010cController extends Controller {
    var $ModelClassName = "knje010cModel";
    var $ProgramID      = "KNJE010C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "reload3": //RECORD_TOTALSTUDYTIME_DAT (通知書) より読込む
                case "edit":
                case "yomikomi":
                    $this->callView("knje010cForm1");
                    break 2;
                case "reload2":  //学習指導要録より読込
                case "reload4":  //通知書より読込
                case "torikomi3":
                case "form2_first": //「出欠の～」の最初の呼出
                case "form2": //出欠の～
                    $this->callView("knje010cForm2");
                    break 2;
                case "form3_first": //「成績参照」の最初の呼出
                case "form3": //「成績参照」
                    $this->callView("knje010cSubForm1");
                    break 2;
                case "form4_first": //「指導要録参照」の最初の呼出
                case "form4": //「指導要録参照」
                    $this->callView("knje010cSubForm2");
                    break 2;
                case "form6_first": //「指導要録参照」の最初の呼出
                case "form6": //「指導要録参照」
                    $this->callView("knje010cSubForm6");
                    break 2;
                case "subform3": //部活参照
                    $this->callView("knje010cSubForm3");
                    break 2;
                case "subform4": //委員会参照
                    $this->callView("knje010cSubForm4");
                    break 2;
                case "subform5": //資格参照
                    $this->callView("knje010cSubForm5");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje010cForm1");
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje010cForm2");
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "reload":  //保健より読み込み
                    $sessionInstance->getReloadHealthModel();
                    break 2;
                case "reset":
                    $this->callView("knje010cForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE010C/knje010cindex.php?cmd=edit") ."&button=3";
                    $args["right_src"] = "knje010cindex.php?cmd=edit&init=1";
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
$knje010cCtl = new knje010cController;
//var_dump($_REQUEST);
?>
