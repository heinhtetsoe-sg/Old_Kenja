<?php

require_once('for_php7.php');

require_once('knje066Model.inc');
require_once('knje066Query.inc');

class knje066Controller extends Controller {
    var $ModelClassName = "knje066Model";
    var $ProgramID      = "KNJE066";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "right_list":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje066Form1");
                    break 2;
                case "reset":
                case "edit_select":
                case "edit_src":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje066Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit_src");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit_src");
                    break 1;
                case "top_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateTopModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("right_list");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit_src");
                    break 1;
                case "top_delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteTopModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("right_list");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "call":
                    //分割フレーム作成
                    $args["left_src"]  = "";
                    
                    $args["right_src"] = "knje066index.php?cmd=right_list";
                    $args["edit_src"]  = "knje066index.php?cmd=edit_src";
                    $args["cols"] = "0%,*";
                    $args["rows"] = "35%,*";
                    View::frame($args, "frame2.html");

                    exit;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP4/index.php?";
                    $args["left_src"] .= "PROGRAMID=".$this->ProgramID;
                    $args["left_src"] .= "&AUTH=".AUTHORITY;
                    $args["left_src"] .= "&search_div=1";
                    $args["left_src"] .= "&search_tenhen=1";
                    $args["left_src"] .= "&name=1";
                    $args["left_src"] .= "&name_kana=1";
                    $args["left_src"] .= "&ent_year=1";
                    $args["left_src"] .= "&grd_year=1";
                    $args["left_src"] .= "&schno=1";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["left_src"] .= "&PATH=" .urlencode("/E/KNJE066/knje066index.php?cmd=edit");

                    $args["right_src"] = "knje066index.php?cmd=right_list";
                    $args["edit_src"]  = "knje066index.php?cmd=edit_src";
                    $args["cols"] = "22%,*";
                    $args["rows"] = "55%,*";
                    View::frame($args, "frame2.html");

                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJE066Ctl = new knje066Controller;
//var_dump($_REQUEST);

?>
