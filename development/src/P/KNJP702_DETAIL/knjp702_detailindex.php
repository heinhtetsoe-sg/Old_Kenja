<?php

require_once('for_php7.php');

require_once('knjp702_detailModel.inc');
require_once('knjp702_detailQuery.inc');

class knjp702_detailController extends Controller {
    var $ModelClassName = "knjp702_detailModel";
    var $ProgramID      = "KNJP702_DETAIL";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "main":
                    $sessionInstance->knjp702_detailModel();      //コントロールマスタの呼び出し
                    $this->callView("knjp702_detailForm1");
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
$knjp702_detailCtl = new knjp702_detailController;
//var_dump($_REQUEST);
?>
