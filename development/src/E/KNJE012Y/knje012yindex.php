<?php

require_once('for_php7.php');
require_once('knje012yModel.inc');
require_once('knje012yQuery.inc');

class knje012yController extends Controller {
    var $ModelClassName = "knje012yModel";
    var $ProgramID      = "KNJE012Y";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "reset":
                case "updEdit":
                case "edit":
                    $this->callView("knje012yForm1");
                    break 2;
                case "subform1":    //通知表所見参照
                    $this->callView("knje012ySubForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/E/KNJE012Y/knje012yindex.php?cmd=edit") ."&button=3";
                    $args["right_src"] = "knje012yindex.php?cmd=edit&init=1";
                    $args["cols"] = "20%,*";
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
$knje012yCtl = new knje012yController;
//var_dump($_REQUEST);
?>
