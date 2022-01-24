<?php

require_once('for_php7.php');

require_once('knjp190qModel.inc');
require_once('knjp190qQuery.inc');

class knjp190qController extends Controller {
    var $ModelClassName = "knjp190qModel";
    var $ProgramID      = "knjp190q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                    $this->callView("knjp190qForm2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjp190qForm1");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":

                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/P/KNJP190Q/knjp190qindex.php?cmd=right_list") ."&button=1";
                    $args["right_src"] = "knjp190qindex.php?cmd=right_list";
                    $args["edit_src"]  = "knjp190qindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
#                    $args["cols"] = "24%,76%";
                    $args["rows"] = "50%,50%";
                    View::frame($args,"frame2.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp190qCtl = new knjp190qController;
?>
