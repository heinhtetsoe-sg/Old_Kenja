<?php

require_once('for_php7.php');
require_once('knje012aModel.inc');
require_once('knje012aQuery.inc');

class knje012aController extends Controller {
    var $ModelClassName = "knje012aModel";
    var $ProgramID      = "KNJE012A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "reload3": //RECORD_TOTALSTUDYTIME_DAT (通知書) より読込む
                case "updEdit":
                case "edit":
                    $this->callView("knje012aForm1");
                    break 2;
                case "reload2":  //学習指導要録より読込
                case "reload4":  //通知書より読込
                case "form2_first": //「出欠の～」の最初の呼出
                case "form2": //出欠の～
                    $this->callView("knje012aForm2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje012aForm1");
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "reset":
                    $this->callView("knje012aForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/E/KNJE012A/knje012aindex.php?cmd=edit") ."&button=3";
                    $args["right_src"] = "knje012aindex.php?cmd=edit&init=1";
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
$knje012aCtl = new knje012aController;
//var_dump($_REQUEST);
?>
