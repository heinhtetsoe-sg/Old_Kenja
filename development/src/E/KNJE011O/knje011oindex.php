<?php

require_once('for_php7.php');

require_once('knje011oModel.inc');
require_once('knje011oQuery.inc');

class knje011oController extends Controller {
    var $ModelClassName = "knje011oModel";
    var $ProgramID      = "KNJE011O";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knje011oForm1");
                    break 2;
                case "reload2":  //学習指導要録より読込
                case "form2_first": //「出欠の～」の最初の呼出
                case "form2": //出欠の～
                    $this->callView("knje011oForm2");
                    break 2;
                case "form3_first": //「成績参照」の最初の呼出
                case "form3": //「成績参照」
                    $this->callView("knje011oSubForm1");
                    break 2;
                case "form4_first": //「指導要録参照」の最初の呼出
                case "form4": //「指導要録参照」
                    $this->callView("knje011oSubForm2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011oForm1");
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011oForm2");
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "reload":  //保健より読み込み
                    $sessionInstance->getReloadHealthModel();
                    break 2;
                case "reset":
                    $this->callView("knje011oForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == ""){
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE011O/knje011oindex.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode($programpath."/knje011oindex.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["right_src"] = "knje011oindex.php?cmd=edit&init=1";
                    $args["cols"] = "25%,*";
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
$knje011oCtl = new knje011oController;
?>
