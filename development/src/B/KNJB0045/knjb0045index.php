<?php

require_once('for_php7.php');

require_once('knjb0045Model.inc');
require_once('knjb0045Query.inc');

class knjb0045Controller extends Controller {
    var $ModelClassName = "knjb0045Model";
    var $ProgramID      = "KNJB0045";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    //日付のチェック（年度・学期）および動作状態チェック
                    $sessionInstance->CheckDate();
                    //詳細画面に戻る
                    $sessionInstance->setCmd("knjb0045");
                    break 1;
                case "print":
                    //印刷データの存在チェック
                    $sessionInstance->CheckPrint();
                    //詳細画面に戻る
                    $sessionInstance->setCmd("knjb0045");
                    break 1;
                case "knjb0045":
                    $this->callView("knjb0045Form1");
                    exit;
                case "":
                    $sessionInstance->knjb0045Model();
                    $this->callView("knjb0045Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb0045Ctl = new knjb0045Controller;
//var_dump($_REQUEST);
?>
