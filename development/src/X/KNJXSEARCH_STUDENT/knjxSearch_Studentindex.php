<?php

require_once('for_php7.php');

require_once('knjxSearch_StudentModel.inc');
require_once('knjxSearch_StudentQuery.inc');

class knjxSearch_StudentController extends Controller {
    var $ModelClassName = "knjxSearch_StudentModel";
    var $ProgramID      = "KNJXSEARCH_STUDENT";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $this->callView("knjxSearch_StudentForm1");
                    break 2;
                case "search_view": //検索画面
                    $this->callView("knjxSearch_Student");
                    break 2;
                case "":
                    $this->callView("knjxSearch_StudentForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxSearch_StudentCtl = new knjxSearch_StudentController;
//var_dump($_REQUEST);
?>
