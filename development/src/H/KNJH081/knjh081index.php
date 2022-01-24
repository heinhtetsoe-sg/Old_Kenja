<?php
require_once('knjh081Model.inc');
require_once('knjh081Query.inc');

class knjh081Controller extends Controller {
    var $ModelClassName = "knjh081Model";
    var $ProgramID      = "KNJH081";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh081":
                    $sessionInstance->knjh081Model();        //コントロールマスタの呼び出し
                    $this->callView("knjh081Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getCsvModel()){
                        $this->callView("knjh081Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh081Ctl = new knjh081Controller;
//var_dump($_REQUEST);
?>
