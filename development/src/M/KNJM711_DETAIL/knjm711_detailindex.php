<?php

require_once('for_php7.php');

require_once('knjm711_detailModel.inc');
require_once('knjm711_detailQuery.inc');

class knjm711_detailController extends Controller {
    var $ModelClassName = "knjm711_detailModel";
    var $ProgramID      = "KNJM711_DETAIL";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "main":
                    $sessionInstance->knjm711_detailModel();      //コントロールマスタの呼び出し
                    $this->callView("knjm711_detailForm1");
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
$knjm711_detailCtl = new knjm711_detailController;
//var_dump($_REQUEST);
?>
