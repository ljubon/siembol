import { ChangeDetectionStrategy, ChangeDetectorRef, Component, TemplateRef } from "@angular/core";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { MatTableDataSource } from "@angular/material/table";
import { Application, applicationManagerColumns, displayedApplicationManagerColumns } from "@app/model/config-model";
import { EditorService } from "@app/services/editor.service";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 're-application-dialog',
  styleUrls: ['application-dialog.component.scss'],
  templateUrl: 'application-dialog.component.html',
})
export class ApplicationDialogComponent {
  dialogrefAttributes: MatDialogRef<any>;
  dataSource: MatTableDataSource<Application>;
  columns = applicationManagerColumns;
  displayedColumns = displayedApplicationManagerColumns;
  restartedApplications: string[] = [];
  
  constructor(
    private dialogref: MatDialogRef<ApplicationDialogComponent>,
    private service: EditorService,
    private dialog: MatDialog,
    private cd: ChangeDetectorRef
  ) {
    this.service.configLoader.getApplications().subscribe(a => {
      this.createTable(a);
    })
  }

  onClickClose() {
    this.dialogref.close();
  }

  onRestartApplication(applicationName: string, templateRef: TemplateRef<any>) {
    this.service.configLoader.restartApplication(applicationName).subscribe(a => {
      this.createTable(a);
      this.restartedApplications.push(applicationName);
    })
    this.dialogrefAttributes = this.dialog.open(
      templateRef, 
      { 
        data: applicationName,
        maxWidth: '800px',
      });
    
  }

  onViewAttributes(attributes: string[], templateRef: TemplateRef<any>) {
    this.dialogrefAttributes = this.dialog.open(
      templateRef, 
      { 
        data: attributes.map(a => JSON.parse(atob(a))), 
      });
  }

  onClickCloseAttributes() {
    this.dialogrefAttributes.close();
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  private createTable(a: Application[]) {
    const filter = this.dataSource?.filter;
    this.dataSource = new MatTableDataSource(a);
    this.dataSource.filter = filter;
    this.cd.markForCheck();
  }
}