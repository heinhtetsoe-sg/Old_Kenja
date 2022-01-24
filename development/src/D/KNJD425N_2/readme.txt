$Id: readme.txt 76126 2020-08-21 04:22:20Z ooshiro $

2020/03/18  1.作成中

2020/03/25  1.「実態」「目指したい自立の姿」参照機能追加
            --v_challenged_assessment_status_growup_dat.sql(rev. 73242)

2020/04/03  1.新規作成

2020/04/08  1.GRADE_KIND_SCHREG_GROUP_DAT.SEMESTER = '9'に修正

2020/04/10  1.自動入力制御処理を削除

2020/06/04  1.更新処理を修正
　　　　　　 --修正前：表示されている科目がテーブルにあればUPDATE、なければINSERT
　　　　　　 　修正後：DELETEし、INSERT
　　　　　　2.更新処理修正

2020/06/29  1.以下の修正をした
            --「実態参照」「目指したい自立の姿参照」「前年度年間まとめ」のテキストエリアを入力不可に修正

2020/08/21  1.「実態参照」枠数の取得を「CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT」帳票パターン(SHEET_PATTERN)から取得するよう修正
