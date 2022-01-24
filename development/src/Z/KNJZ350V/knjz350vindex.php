<?php

require_once('for_php7.php');

require_once('knjz350vModel.inc');
require_once('knjz350vQuery.inc');

class knjz350vController extends Controller {
    var $ModelClassName = "knjz350vModel";
    var $ProgramID      = "KNJZ350V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "update": //パーツ
                case "update2"://管理者コントロール
                case "update3"://出欠入力コントロール
                case "update4"://実力テスト入力コントロール
                case "updateJview"://観点入力コントロール
                case "updateMypDp"://ＭＹＰ・ＤＰ成績入力コントロール
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                case "subclasscd";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz350vForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz350vCtl = new knjz350vController;
//var_dump($_REQUEST);
?>
