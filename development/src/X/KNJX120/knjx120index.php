<?php

require_once('for_php7.php');

require_once('knjx120Model.inc');
require_once('knjx120Query.inc');

class knjx120Controller extends Controller {
    var $ModelClassName = "knjx120Model";
    var $ProgramID      = "knjx120";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
				case "csv_error":   //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjx120Form1");
					}
					break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx120Form1");
                    break 2;
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
$knjx120Ctl = new knjx120Controller;
?>
