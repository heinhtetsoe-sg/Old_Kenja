<?php

require_once('for_php7.php');

require_once('knjp391kModel.inc');
require_once('knjp391kQuery.inc');

class knjp391kController extends Controller {
    var $ModelClassName = "knjp391kModel";
    var $ProgramID      = "KNJp391k";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjp391k");
                    break 1;
                case "":
                case "knjp391k":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp391kModel();      //コントロールマスタの呼び出し
                    $this->callView("knjp391kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp391kCtl = new knjp391kController;
var_dump($_REQUEST);
?>
