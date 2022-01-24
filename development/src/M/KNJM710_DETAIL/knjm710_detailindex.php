<?php

require_once('for_php7.php');

require_once('knjm710_detailModel.inc');
require_once('knjm710_detailQuery.inc');

class knjm710_detailController extends Controller {
    var $ModelClassName = "knjm710_detailModel";
    var $ProgramID      = "KNJM710_DETAIL";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "main":
                    $sessionInstance->knjm710_detailModel();      //コントロールマスタの呼び出し
                    $this->callView("knjm710_detailForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm710_detailCtl = new knjm710_detailController;
//var_dump($_REQUEST);
?>
