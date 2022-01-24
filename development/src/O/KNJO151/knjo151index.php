<?php
require_once('knjo151Model.inc');
require_once('knjo151Query.inc');

class knjo151Controller extends Controller {
    var $ModelClassName = "knjo151Model";
    var $ProgramID      = "KNJO151";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "right_list":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjo151Form1");
                    break 2;
                case "edit_select":
                case "edit_src":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjo151Form2");
                    break 2;
                case "preupdate":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getPreUpdateModel();
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
                case "error":
                    $this->callView("error");
                    break 2;
                case "call":
                    //分割フレーム作成
                    $args["left_src"]  = "";
                    
                    $args["right_src"] = "knjo151index.php?cmd=right_list";
                    $args["edit_src"]  = "knjo151index.php?cmd=edit_src";
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
                    $args["left_src"] .= "&PATH=" .urlencode("/O/KNJO151/knjo151index.php?cmd=edit");

                    $args["right_src"] = "knjo151index.php?cmd=right_list";
                    $args["edit_src"]  = "knjo151index.php?cmd=edit_src";
                    $args["cols"] = "25%,*";
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
$KNJo151Ctl = new knjo151Controller;
//var_dump($_REQUEST);

?>
