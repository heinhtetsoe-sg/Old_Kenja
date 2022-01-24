<?php

require_once('for_php7.php');

require_once('knja220Model.inc');
require_once('knja220Query.inc');

class knja220Controller extends Controller {
    var $ModelClassName = "knja220Model";
    var $ProgramID      = "KNJA220";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja220":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja220Model();		//コントロールマスタの呼び出し
                    $this->callView("knja220Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja220Ctl = new knja220Controller;
var_dump($_REQUEST);
?>
