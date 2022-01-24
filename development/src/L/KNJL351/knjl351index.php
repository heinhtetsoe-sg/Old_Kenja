<?php

require_once('for_php7.php');

require_once('knjl351Model.inc');
require_once('knjl351Query.inc');

class knjl351Controller extends Controller {
    var $ModelClassName = "knjl351Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl351":
                    $sessionInstance->knjl351Model();
                    $this->callView("knjl351Form1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjl351Form1");
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
$knjl351Ctl = new knjl351Controller;
//var_dump($_REQUEST);
?>
