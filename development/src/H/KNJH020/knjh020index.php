<?php

require_once('for_php7.php');
require_once('knjh020Model.inc');
require_once('knjh020Query.inc');

class knjh020Controller extends Controller {
    var $ModelClassName = "knjh020Model";
	var $ProgramID      = "KNJH020";


    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knjh020Form1");
                    break 2;
                case "update":
					//NO001
					if (!$sessionInstance->auth){
	                    $this->checkAuth(DEF_UPDATE_RESTRICT);
					}
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
					//NO001
					if (!$sessionInstance->auth){
	                    $this->checkAuth(DEF_UPDATE_RESTRICT);
					}
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "add":
					//NO001
					if (!$sessionInstance->auth){
	                    $this->checkAuth(DEF_UPDATE_RESTRICT);
					}
                    $sessionInstance->getAddingModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH020/knjh020index.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjh020index.php?cmd=edit";
                    $args["cols"] = "22%,*%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}で!す"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjh020Ctl = new knjh020Controller;
//var_dump($_REQUEST);
?>
