<?php

require_once('for_php7.php');

require_once('knjl031cModel.inc');
require_once('knjl031cQuery.inc');

class knjl031cController extends Controller {
    var $ModelClassName = "knjl031cModel";
    var $ProgramID      = "KNJL031C";

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
                case "knjl031cForm1":                    //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl031cModel();   //コントロールマスタの呼び出し
                    $this->callView("knjl031cForm1");
                    exit;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "read";
                case "clear";
                    $this->callView("knjl031cForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl031cCtl = new knjl031cController;
//var_dump($_REQUEST);
?>
