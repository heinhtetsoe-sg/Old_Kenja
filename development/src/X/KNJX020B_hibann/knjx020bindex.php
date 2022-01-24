<?php

require_once('for_php7.php');

require_once('knjx020bModel.inc');
require_once('knjx020bQuery.inc');

class knjx020bController extends Controller {
    var $ModelClassName = "knjx020bModel";
    var $ProgramID      = "KNJX020B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":            //データ取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv_data":        //データ出力
                    if (!$sessionInstance->getDataModel()){
                        $this->callView("knjx020bForm1");
                    }
                    break 2;
                case "csv_error":       //エラー出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjx020bForm1");
                    }
                    break 2;
                case "csv_header":      //ヘッダ出力
                    if (!$sessionInstance->getHeaderModel()){
                        $this->callView("knjx020bForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx020bForm1");
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
$knjx020bCtl = new knjx020bController;
?>
