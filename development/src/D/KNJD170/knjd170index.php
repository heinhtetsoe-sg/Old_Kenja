<?php

require_once('for_php7.php');

require_once('knjd170Model.inc');
require_once('knjd170Query.inc');

class knjd170Controller extends Controller {
    var $ModelClassName = "knjd170Model";
    var $ProgramID      = "KNJD170";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd170":								//メニュー画面もしくはSUBMITした場合
                case "clickcheng":							//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd170Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd170Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjd170Form1");
					}
					break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd170Ctl = new knjd170Controller;
//var_dump($_REQUEST);
?>
