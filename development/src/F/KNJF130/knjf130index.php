<?php

require_once('for_php7.php');

require_once('knjf130Model.inc');
require_once('knjf130Query.inc');

class knjf130Controller extends Controller {
    var $ModelClassName = "knjf130Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf130":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjf130Model();		//コントロールマスタの呼び出し
                    $this->callView("knjf130Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjf130Form1");
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
$knjf130Ctl = new knjf130Controller;
//var_dump($_REQUEST);
?>
