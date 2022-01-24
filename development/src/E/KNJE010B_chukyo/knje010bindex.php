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
                case "edit":
                case "reload2":
                case "reload3":
                    $this->callView("knje010bForm1");
                    break 2;
                case "form3_first": //「成績参照」の最初の呼出
                case "form3": //「成績参照」
                    $this->callView("knje010bSubForm1");
                    break 2;
                case "form4_first": //「指導要録参照」の最初の呼出
                case "form4": //「指導要録参照」
                    $this->callView("knje010bSubForm2");
                    break 2;
                case "subform3": //部活参照
                    $this->callView("knje010bSubForm3");
                    break 2;
                case "subform4": //委員会参照
                    $this->callView("knje010bSubForm4");
                    break 2;
                case "subform5": //資格参照
                    $this->callView("knje010bSubForm5");
                    break 2;
                case "subform6": //旧調査書の指導上参照となる諸事項
                    $this->callView("knje010bSubForm6");
                    break 2;
                case "form7_first": //「指導要録参照」の最初の呼出
                case "form7": //「指導要録参照」
                    $this->callView("knje010bSubForm7");
                    break 2;
                case "subform8":    //記録備考参照
                    $this->callView("knje010bSubForm8");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje010bForm1");
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reload":  //保健より読み込み
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
