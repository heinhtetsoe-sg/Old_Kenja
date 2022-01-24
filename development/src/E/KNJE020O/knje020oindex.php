<?php

require_once('for_php7.php');

require_once('knje020oModel.inc');
require_once('knje020oQuery.inc');

class knje020oController extends Controller {
    var $ModelClassName = "knje020oModel";
    var $ProgramID      = "KNJE020O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {

            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knje020oForm1");
                    break 2;
                case "subform1": //成績参照
                    $this->callView("knje020oSubForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje020oForm1");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    break 2;
                case "detail":
                    break 2;
                case "reset":
                    $this->callView("knje020oForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == ""){
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE020O/knje020oindex.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode($programpath."/knje020oindex.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["right_src"] = "knje020oindex.php?cmd=edit&init=1";
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
$knje020oCtl = new knje020oController;
?>
