<?php

require_once('for_php7.php');

require_once('knjl031hModel.inc');
require_once('knjl031hQuery.inc');

class knjl031hController extends Controller {
    var $ModelClassName = "knjl031hModel";
    var $ProgramID      = "KNJL031H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("read");
                    break 1;
                case "knjl031hForm1":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl031hModel();		        //コントロールマスタの呼び出し
                    $this->callView("knjl031hForm1");
                    exit;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "read";
                case "clear";
                    $this->callView("knjl031hForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl031hCtl = new knjl031hController;
//var_dump($_REQUEST);
?>
