<?php

require_once('for_php7.php');
require_once('knjf010Model.inc');
require_once('knjf010Query.inc');

class knjf010Controller extends Controller {
    var $ModelClassName = "knjf010Model";
    var $ProgramID      = "KNJF010";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjf010Form1");
                    break 2;
                case "replace1":
                    $this->callView("knjf010SubForm1");
                    break 2;
                case "replace_update1":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->ReplaceModel1();
                    $sessionInstance->setCmd("replace1");
                    break 1;
                case "replace2":
                    $this->callView("knjf010SubForm2");
                    break 2;
                case "replace_update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->ReplaceModel2();
                    $sessionInstance->setCmd("replace2");
                    break 1;
                case "replace3":
                    $this->callView("knjf010SubForm3");
                    break 2;
                case "replace_update3":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->ReplaceModel3();
                    $sessionInstance->setCmd("replace3");
                    break 1;
                case "replace4":
                    $this->callView("knjf010SubForm4");
                    break 2;
                case "replace_update4":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->ReplaceModel4();
                    $sessionInstance->setCmd("replace4");
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
                case "reset":
                    $this->callView("knjf010Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/F/KNJF010/knjf010index.php?cmd=edit") ."&button=1";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjf010index.php?cmd=edit";
                    $args["cols"] = "19%,81%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf010Ctl = new knjf010Controller;
?>
