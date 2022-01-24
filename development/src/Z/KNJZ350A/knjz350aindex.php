<?php

require_once('for_php7.php');

require_once('knjz350aModel.inc');
require_once('knjz350aQuery.inc');

class knjz350aController extends Controller {
    var $ModelClassName = "knjz350aModel";
    var $ProgramID      = "KNJZ350A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update": //パーツ
                case "update2"://管理者コントロール
                case "update3"://出欠入力コントロール
                case "update4"://実力テスト入力コントロール
                case "updateJview"://観点入力コントロール
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $this->callView("knjz350aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz350aCtl = new knjz350aController;
//var_dump($_REQUEST);
?>
