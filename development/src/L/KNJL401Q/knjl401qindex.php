<?php

require_once('for_php7.php');

require_once('knjl401qModel.inc');
require_once('knjl401qQuery.inc');

class knjl401qController extends Controller {
    var $ModelClassName = "knjl401qModel";
    var $ProgramID      = "KNJL401Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    if($sessionInstance->getInsertModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("");
                        break 1;
                    }else{
                        $this->callView("knjl401qForm1");
                        break 2;
                    }
                case "update":
                    if($sessionInstance->getUpdateModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("");
                        break 1;
                    }else{
                        $this->callView("knjl401qForm1");
                        break 2;
                    }
                case "cancel":
                case "search":
                case "ban":
                case "":
                    $this->callView("knjl401qForm1");
                    break 2;
                case "name_search":
                case "name_search_search":
                    $this->callView("knjl401q_search");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl401qCtl = new knjl401qController;
//var_dump($_REQUEST);
?>
